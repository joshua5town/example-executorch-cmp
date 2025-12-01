package com.example.go_emotions.domain

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module
import org.pytorch.executorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.math.exp

// Constants (CRITICAL: Must match your TFLite model)
private const val PADDING_ID = 1 // Use the correct ID for padding, usually 1 for RoBERTa
private const val CLS_ID: Long = 0 // RoBERTa's <s> (BOS) token ID
private const val SEP_ID: Long = 2 // RoBERTa's </s> (EOS) token ID
private const val MODEL_FIXED_LENGTH = 512

private val EMOTION_LABELS = mapOf(
    0 to "admiration", 1 to "amusement", 2 to "anger", 3 to "annoyance", 4 to "approval", 5 to "caring", 6 to "confusion",
    7 to "curiosity", 8 to "desire", 9 to "disappointment", 10 to "disapproval", 11 to "disgust", 12 to "embarrassment",
    13 to "excitement", 14 to "fear", 15 to "gratitude", 16 to "grief", 17 to "joy", 18 to "love", 19 to "nervousness",
    20 to "optimism", 21 to "pride", 22 to "realization", 23 to "relief", 24 to "remorse", 25 to "sadness", 26 to "surprise",
    27 to "neutral"
)

// Define the output structure
data class TokenizationResult(
    val inputIds: LongArray,
    val attentionMask: LongArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TokenizationResult

        if (!inputIds.contentEquals(other.inputIds)) return false
        if (!attentionMask.contentEquals(other.attentionMask)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inputIds.hashCode()
        result = 31 * result + attentionMask.contentHashCode()
        return result
    }
}

actual class EmotionPrediction {

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.cacheDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        try {
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    actual suspend fun predict(text: String): String? {
        val context = AppContext.get()

        try {

            // 1. DEFINE THE EXPECTED FILE NAMES
            val modelName = "go_emotions_xnnpack.pte"
            val tokenizerName = "tokenizer.json"

            // 2. POINT TO ASSETS ON DISK
            val pt_model_path = assetFilePath(context, modelName)
            val json_tokenizer_path = assetFilePath(context, tokenizerName)

            // 4. Tokenize the Input Text
            val tokenizationResult = tokenizerUtil(
                text = text,
                jsonTokenizerPath = json_tokenizer_path
            )

            val pt_model = Module.load(pt_model_path)
            // --- Input 0: input_ids ---
            val inputIdsTensor = Tensor.fromBlob(tokenizationResult.inputIds, longArrayOf(1, 512))

            // --- Input 1: attention_mask (The Missing Tensor) ---
            // You must calculate this array based on the length of your actual tokens.
            // For a fully padded example (all 512 tokens are real), it would be all 1s.
            val attentionMaskTensor = Tensor.fromBlob(tokenizationResult.attentionMask, longArrayOf(1, 512))

            // --- Corrected Forward Call ---
            // Pass both tensors as separate EValue arguments

            val outputs = pt_model.forward(
                EValue.from(inputIdsTensor),  // This is Input 0
                EValue.from(attentionMaskTensor) // This is Input 1 (The fix!)
            )

            // 1. Extract the main output EValue (usually the first element)
            val logitsEValue = outputs[0]

            // 2. Convert the EValue to a Tensor
            val logitsTensor: Tensor = logitsEValue.toTensor()

            // 3. Convert the Tensor data to a float array (logits are typically float32)
            // NOTE: .toFloatArray() is a common helper method in mobile runtimes.
            // If that doesn't exist, use .getDataAsFloatArray() or similar.
            val logitsArray: FloatArray = logitsTensor.dataAsFloatArray

            // 4. Post-Process (Softmax and Argmax)

            // Apply Softmax manually to get probabilities (optional, but good for confidence)
            val expLogits = logitsArray.map { exp(it) }.toFloatArray()
            val sumExpLogits = expLogits.sum()
            val probabilities = expLogits.map { it / sumExpLogits }.toFloatArray()

            // 4. Create a list of pairs (Index, Probability)
            val indexedProbabilities = probabilities.mapIndexed { index, probability ->
                Pair(index, probability)
            }

            // 5. Sort the list in descending order based on probability
            val sortedProbabilities = indexedProbabilities.sortedByDescending { it.second }

            // 6. Get the Top 2 Results
            val top1Result = sortedProbabilities.getOrNull(0) // Safe access for the first element
            val top2Result = sortedProbabilities.getOrNull(1) // Safe access for the second element

            // --- Output Formatting ---
            val topEmotions = mutableListOf<String>()

            // Top 1 Emotion
            if (top1Result != null) {
                val id = top1Result.first
                val probability = top1Result.second
                val emotion = EMOTION_LABELS[id]

                topEmotions.add("1st: $emotion (Confidence: ${String.format("%.2f", probability * 100)}%)")
            }

            // Top 2 Emotion
            if (top2Result != null) {
                val id = top2Result.first
                val probability = top2Result.second
                val emotion = EMOTION_LABELS[id]

                topEmotions.add("2nd: $emotion (Confidence: ${String.format("%.2f", probability * 100)}%)")
            }

            // Result will be a list of formatted strings, e.g., ["1st: joy (95.10%)", "2nd: excitement (3.50%)"]
            return topEmotions.joinToString(separator = "\n")
        }catch (e: Exception){
            e.printStackTrace()
            return "Error"
        }
    }

    // --- SIMPLIFIED TOKENIZATION (FOR DEMO ONLY) ---
    // This handles basic spacing and the 'Ġ' prefix but skips subword splitting.
    private fun simpleRobertaTokenize(text: String, vocabMap: Map<String, Long>): List<Long> {
        val tokens = mutableListOf<Long>()

        // 1. Split by space (simulates the initial RoBERTa split)
        val words = text.split(" ")

        words.forEachIndexed { index, word ->
            if (word.isNotEmpty()) {
                var lookupToken = word

                // 2. Add 'Ġ' prefix (except for the first token)
                if (index > 0) {
                    lookupToken = "Ġ$word" // 'Ġ' character (U+0120)
                }

                // 3. Simple lookup
                val tokenId = vocabMap[lookupToken]
                if (tokenId != null) {
                    tokens.add(tokenId)
                } else {
                    // FALLBACK: If the word isn't found, try to find a known part or use UNK.
                    // NOTE: Proper BPE splitting must happen here!
                    tokens.add(vocabMap["<unk>"] ?: 3) // Assuming <unk> is ID 3
                }
            }
        }
        return tokens
    }

    private fun padAndTruncate(tokenIds: List<Long>): TokenizationResult {
        // 1. Create arrays of exactly 512 length (or MODEL_FIXED_LENGTH)
        // Initialize inputIds with the PADDING_ID (usually 1)
        val paddedIds = LongArray(MODEL_FIXED_LENGTH) { PADDING_ID.toLong() }

        // Initialize attentionMask with 0 (indicating padding)
        val attentionMask = LongArray(MODEL_FIXED_LENGTH) { 0L }

        // 2. Calculate how many tokens to actually copy (truncate if too long)
        val copyLength = minOf(tokenIds.size, MODEL_FIXED_LENGTH)

        // 3. Fill in the actual data
        for (i in 0 until copyLength) {
            paddedIds[i] = tokenIds[i].toLong() // Convert Int token to Long
            attentionMask[i] = 1L               // Mark this position as a real token
        }

        // The rest of the array indices (from copyLength to 511) remain as initialized:
        // paddedIds -> PADDING_ID
        // attentionMask -> 0L

        return TokenizationResult(
            inputIds = paddedIds,
            attentionMask = attentionMask
        )
    }

    private fun tokenizerUtil(
        text: String,
        jsonTokenizerPath: String,
    ): TokenizationResult {
        val tokenizerFile = File(jsonTokenizerPath)

        // 1. Load and Parse the Vocabulary JSON from the file system
        val vocabMap: Map<String, Long> = try {
            java.io.FileInputStream(tokenizerFile).use { inputStream ->
                // Use UTF_8, not UTF_16
                InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->

                    // A. Parse the root as a generic JsonObject first
                    val rootObject = Gson().fromJson(reader, JsonObject::class.java)

                    // B. Check for the standard Hugging Face structure: root -> "model" -> "vocab"
                    if (rootObject.has("model")) {
                        val modelObject = rootObject.getAsJsonObject("model")
                        val vocabObject = modelObject.getAsJsonObject("vocab")

                        // Extract just the "vocab" part into our Map
                        val type = object : TypeToken<Map<String, Long>>() {}.type
                        Gson().fromJson(vocabObject, type)
                    } else {
                        // Fallback: Maybe it IS a simple flat vocab.json file?
                        val type = object : TypeToken<Map<String, Long>>() {}.type
                        Gson().fromJson(rootObject, type)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error parsing tokenizer JSON: ${e.message}")
            return TokenizationResult(LongArray(MODEL_FIXED_LENGTH), LongArray(MODEL_FIXED_LENGTH))
        }

        // 2. Perform RoBERTa Tokenization
        val rawTokenIds = simpleRobertaTokenize(text, vocabMap)

        // 3. Add Special Tokens [CLS] ... [SEP]
        val specialTokensList = mutableListOf<Long>()
        specialTokensList.add(CLS_ID)
        specialTokensList.addAll(rawTokenIds)
        specialTokensList.add(SEP_ID)

        // 4. Pad/Truncate and Return
        return padAndTruncate(specialTokensList)
    }
}