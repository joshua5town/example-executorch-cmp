# Store ai-edge-torch google way

package com.example.go_emotions.domain

import android.content.Context
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.math.exp

// Constants (CRITICAL: Must match your TFLite model)
private const val PADDING_ID = 1 // Use the correct ID for padding, usually 1 for RoBERTa
private const val CLS_ID = 0 // RoBERTa's <s> (BOS) token ID
private const val SEP_ID = 2 // RoBERTa's </s> (EOS) token ID
private const val BATCH_SIZE = 2 // Required by your specific TFLite export
private const val NUM_CLASSES = 28
private const val MODEL_FIXED_LENGTH = 514

// Define the output structure
data class TokenizationResult(
val inputIds: IntArray,
val attentionMask: IntArray
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

    actual suspend fun predict(text: String): String? {
        val labels = mapOf(
            0 to "admiration", 1 to "amusement", 2 to "anger", 3 to "annoyance", 4 to "approval", 5 to "caring", 6 to "confusion",
            7 to "curiosity", 8 to "desire", 9 to "disappointment", 10 to "disapproval", 11 to "disgust", 12 to "embarrassment",
            13 to "excitement", 14 to "fear", 15 to "gratitude", 16 to "grief", 17 to "joy", 18 to "love", 19 to "nervousness",
            20 to "optimism", 21 to "pride", 22 to "realization", 23 to "relief", 24 to "remorse", 25 to "sadness", 26 to "surprise",
            27 to "neutral"
        )

        val context = AppContext.get()

        try {
            // Load model and initialize runtime
            val  model =
                CompiledModel.create(
                    context.assets,
                    "go_emotions.tflite",
                    CompiledModel.Options(
                        Accelerator.CPU
                    )

                )

            // Preallocate input/output buffers
            val inputBuffers = model.createInputBuffers()
            val outputBuffers = model.createOutputBuffers()

            val tokenResult = tokenizerUtil(text, context)
            val inputIdsSingle = tokenResult.inputIds // Use this for batching
            val attentionMaskSingle = tokenResult.attentionMask // Use this for batching

            /// --- 4. Prepare Inputs for Batch Size 2 (The CRITICAL Fix) ---
            val L_dyn = inputIdsSingle.size
            val INPUT_ELEMENTS = BATCH_SIZE * L_dyn
            val inputIds = IntArray(INPUT_ELEMENTS)
            val attentionMask = IntArray(INPUT_ELEMENTS)

            // Slot 0 (0 to L_dyn-1)
            System.arraycopy(inputIdsSingle, 0, inputIds, 0, L_dyn)
            System.arraycopy(attentionMaskSingle, 0, attentionMask, 0, L_dyn)

            // Slot 1 (L_dyn to (2*L_dyn)-1)
            System.arraycopy(inputIdsSingle, 0, inputIds, L_dyn, L_dyn)
            System.arraycopy(attentionMaskSingle, 0, attentionMask, L_dyn, L_dyn)

            // --- 5. Write Inputs to Buffers ---

            // Input 0: input_ids
            inputBuffers[0].writeInt(inputIds)

            // Input 1: attention_mask
            // NOTE: You must write BOTH input tensors!
            inputBuffers[1].writeInt(attentionMask)

            // --- 6. Invoke Model ---
            model.run(inputBuffers, outputBuffers)

            // --- 7. Read Output and Post-process ---

            // Read the raw output (logits) for the entire batch
            val outputArray = outputBuffers[0].readFloat() // Returns FloatArray of size 2 * 28 = 56

            // Extract the results for our single input (the first 28 elements)
            val logits = outputArray.sliceArray(0 until NUM_CLASSES)

            // Apply Softmax to convert logits into probabilities
            val expLogits = logits.map { exp(it) }
            val sumExpLogits = expLogits.sum()
            val probabilities = expLogits.map { it / sumExpLogits }

            // Find the index of the highest probability
            val predictedIndex = probabilities.indexOf(probabilities.maxOrNull())

            // Return the corresponding emotion label
            return labels[predictedIndex]
        }catch (e: Exception){
            e.printStackTrace()
            return "Error"
        }
    }

    // --- SIMPLIFIED TOKENIZATION (FOR DEMO ONLY) ---
// This handles basic spacing and the 'Ġ' prefix but skips subword splitting.
private fun simpleRobertaTokenize(text: String, vocabMap: Map<String, Int>): List<Int> {
val tokens = mutableListOf<Int>()

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

    private fun padAndTruncate(tokenIds: List<Int>): TokenizationResult {
        // Final arrays MUST be the fixed length
        val paddedIds = IntArray(MODEL_FIXED_LENGTH) { PADDING_ID }
        val attentionMask = IntArray(MODEL_FIXED_LENGTH) { 0 }

        // Calculate how many tokens to actually copy
        val copyLength = minOf(tokenIds.size, MODEL_FIXED_LENGTH)

        // Copy the tokens up to the fixed or truncated length
        tokenIds.take(copyLength).forEachIndexed { index, id ->
            paddedIds[index] = id
            attentionMask[index] = 1 // Mark as actual token (not padding)
        }

        // After this, paddedIds will have CLS/tokens/SEP, followed by PADDING_ID (1)
        // and attentionMask will have 1s, followed by 0s.

        return TokenizationResult(
            inputIds = paddedIds,
            attentionMask = attentionMask
        )
    }

    private fun tokenizerUtil(
        text: String,
        context: Context
    ): TokenizationResult {
        // 1. Load and Parse the Vocabulary JSON
        val vocabMap: Map<String, Int> = context.assets.open("go_emotions_vocab.json").use { inputStream ->
            InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                val type = object : TypeToken<Map<String, Int>>() {}.type
                Gson().fromJson(reader, type)
            }
        }

        // 2. Perform RoBERTa Tokenization (The Hard Part!)
        // This requires implementing a Byte-Pair Encoding (BPE) split logic
        // specific to RoBERTa.
        val rawTokenIds = simpleRobertaTokenize(text, vocabMap)

        // 3. Add Special Tokens
        // Format: [CLS] tokens... [SEP]
        val specialTokensList = mutableListOf<Int>()
        specialTokensList.add(CLS_ID)
        specialTokensList.addAll(rawTokenIds)
        specialTokensList.add(SEP_ID)
        // 4. Pad/Truncate
        return padAndTruncate(specialTokensList)
    }
}