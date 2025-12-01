//
//  Prediction.swift
//  iosApp
//
//  Created by Joshua Townsend on 9/27/25.
//

import ComposeApp
import SwiftUI
import ExecuTorch
import Darwin
import Foundation

class PredictionServiceIOS: ComposeApp.PredictionService {
    
    static let shared = PredictionServiceIOS()
    // Constants (CRITICAL: Must match your TFLite model)
    static let PADDING_ID: Int64 = 1 // Use the correct ID for padding, usually 1 for RoBERTa
    static let CLS_ID: Int64 = 0 // RoBERTa's <s> (BOS) token ID
    static let SEP_ID: Int64 = 2 // RoBERTa's </s> (EOS) token ID
    static let MODEL_FIXED_LENGTH = 512

    let EMOTION_LABELS: [Int: String] = [
        0: "admiration", 1: "amusement", 2: "anger", 3: "annoyance", 4: "approval", 5: "caring", 6: "confusion",
        7: "curiosity", 8: "desire", 9: "disappointment", 10: "disapproval", 11: "disgust", 12: "embarrassment",
        13: "excitement", 14: "fear", 15: "gratitude", 16: "grief", 17: "joy", 18: "love", 19: "nervousness",
        20: "optimism", 21: "pride", 22: "realization", 23: "relief", 24: "remorse", 25: "sadness", 26: "surprise",
        27: "neutral"
    ]
    
    struct TokenizationResult {
        let inputIds: [Int64]
        let attentionMask: [Int64]

        init?(firebaseDictionary dict: [String: Any]) {
            guard
                let inputIds = dict["inputIds"] as? [Int64],
                let attentionMask = dict["attentionMask"] as? [Int64]
            else {
                return nil
            }

            self.inputIds = inputIds
            self.attentionMask = attentionMask
        }

        init(inputIds: [Int64], attentionMask: [Int64]) {
            self.inputIds = inputIds
            self.attentionMask = attentionMask
        }
    }
    
    func predict(text: String) async throws -> String {
        
        // 1. DEFINE THE EXPECTED FILE NAMES

        guard let ptModelPath = Bundle.main.path(forResource: "go_emotions_coreml", ofType: "pte") else {
            throw MyInferenceError.fileNotFound("go_emotions_xnnpack.pte not found in App Bundle. Check 'Copy Bundle Resources' build phase.")
        }

        guard let jsonTokenizerPath = Bundle.main.path(forResource: "tokenizer", ofType: "json") else {
            throw MyInferenceError.fileNotFound("tokenizer.json not found in App Bundle. Check 'Copy Bundle Resources' build phase.")
        }
        
        do {
            // 2. Tokenize the Input Text
            let tokenizationResult = tokenizerUtil(text: text, jsonTokenizerPath: jsonTokenizerPath)
            
            let pt_model = Module(filePath: ptModelPath)
            
            // --- Input 0: input_ids ---
            let inputIdsTensor = Tensor<Int64>(tokenizationResult.inputIds, shape: [1, PredictionServiceIOS.MODEL_FIXED_LENGTH])

            // --- Input 1: attention_mask (The Missing Tensor) ---
            // You must calculate this array based on the length of your actual tokens.
            // For a fully padded example (all 512 tokens are real), it would be all 1s.
            let attentionMaskTensor = Tensor<Int64>(tokenizationResult.attentionMask, shape: [1, PredictionServiceIOS.MODEL_FIXED_LENGTH])

            // --- Corrected Forward Call ---
            // 3. Pass both tensors as separate EValue arguments
            
            try pt_model.load()
            
            let outputs = try pt_model.forward([inputIdsTensor, attentionMaskTensor])

            print("Inference successful.")

            // 4. Convert the Tensor data to a Int64 array (logits are typically Int64)
            let logitsTensor = try Tensor<Float>(outputs[0].asValue())

            print("scalars successful.")
            
            let logits = logitsTensor.scalars()
            

            // 5. Post-Processing (Softmax and Sorting)

            // Softmax: Calculate exp(logit)
            let expLogits = logits.map { exp(Double($0)) }
            let sumExpLogits = expLogits.reduce(0, +)

            print("Softmax successful.")
            
            // Calculate probabilities
            let probabilities = expLogits.map { $0 / sumExpLogits }

            print("Probabilities successful.")

            // Map to (index, probability) tuples
            let indexedProbabilities: [(index: Int, probability: Double)] = probabilities.enumerated().map { (index, probability) in
                (index: index, probability: probability)
            }

            print("Indexed probabilities successful.")
            
            // Sort descending
            let sortedProbabilities = indexedProbabilities.sorted { $0.probability > $1.probability }

            print("Sorted probabilities successful.")

            // 6. Get Top 2 Results and Format Output
            let top1Result = sortedProbabilities.getOrNil(0) // Safe access using the new extension
            let top2Result = sortedProbabilities.getOrNil(1)

            print("Formatted output successful.")

            var topEmotions = [String]()

            // Helper function to format the output string
            let formatResult: ((index: Int, probability: Double)) -> String = { result in
                let emotion = self.EMOTION_LABELS[result.index] ?? "Unknown"
                let confidence = String(format: "%.2f", result.probability * 100)
                return "\(emotion) (Confidence: \(confidence)%)"
            }

            if let top1 = top1Result {
                topEmotions.append("1st: \(formatResult(top1))")
            }

            if let top2 = top2Result {
                topEmotions.append("2nd: \(formatResult(top2))")
            }

            // Result will be a list of formatted strings, e.g., ["1st: joy (95.10%)", "2nd: excitement (3.50%)"]
            return topEmotions.joined(separator: "\n")
        } catch {
            print("Inference Error:", error)
            return "Error"
        }
    }
    
    private func simpleRobertaTokenize(text: String, vocab: [String: Int64]) -> [Int64] {
        var tokens = [Int64]()
        let words = text.split(separator: " ")
        for (index, wordSub) in words.enumerated() {
            let word = String(wordSub)
            if !word.isEmpty {
                var lookupToken = word
                if index > 0 {
                    lookupToken = "\u{0120}" + word // 'Ġ' character (U+0120)
                }
                if let tokenId = vocab[lookupToken] {
                    tokens.append(tokenId)
                } else {
                    // FALLBACK: If the word isn't found, try to find a known part or use UNK.
                    tokens.append(vocab["<unk>"] ?? 3) // Assuming <unk> is ID 3
                }
            }
        }
        return tokens
    }
    
    private func padAndTruncate(_ tokenIds: [Int64]) -> TokenizationResult {
        // 1. Create arrays of exactly MODEL_FIXED_LENGTH length
        var paddedIds = [Int64](repeating: Self.PADDING_ID, count: Int(Self.MODEL_FIXED_LENGTH))
        var attentionMask = [Int64](repeating: 0, count: Int(Self.MODEL_FIXED_LENGTH))
        let copyLength = min(tokenIds.count, Int(Self.MODEL_FIXED_LENGTH))
        for i in 0..<copyLength {
            paddedIds[i] = tokenIds[i]
            attentionMask[i] = 1
        }
        return TokenizationResult(inputIds: paddedIds, attentionMask: attentionMask)
    }
    
    private func tokenizerUtil(text: String, jsonTokenizerPath: String) -> TokenizationResult {
        let fileManager = FileManager.default
        if !fileManager.fileExists(atPath: jsonTokenizerPath) {
            print("Error: Tokenizer JSON file not found at path: \(jsonTokenizerPath)")
            let dummyArray = [Int64](repeating: 0, count: Int(Self.MODEL_FIXED_LENGTH))
            return TokenizationResult(inputIds: dummyArray, attentionMask: dummyArray)
        }
        do {
            let data = try Data(contentsOf: URL(fileURLWithPath: jsonTokenizerPath))
            let jsonRoot = try JSONSerialization.jsonObject(with: data, options: [])
            var vocabMap: [String: Int64] = [:]
            if let rootObj = jsonRoot as? [String: Any],
               let modelObj = rootObj["model"] as? [String: Any],
               let vocabObj = modelObj["vocab"] as? [String: Any] {
                for (key, value) in vocabObj {
                    if let num = value as? Int64 {
                        vocabMap[key] = num
                    } else if let num = value as? Int {
                        vocabMap[key] = Int64(num)
                    } else if let num = value as? NSNumber {
                        vocabMap[key] = num.int64Value
                    }
                }
            } else if let flatMap = jsonRoot as? [String: Any] {
                for (key, value) in flatMap {
                    if let num = value as? Int64 {
                        vocabMap[key] = num
                    } else if let num = value as? Int {
                        vocabMap[key] = Int64(num)
                    } else if let num = value as? NSNumber {
                        vocabMap[key] = num.int64Value
                    }
                }
            }
            
            // 2. Perform RoBERTa Tokenization
            let rawTokenIds = simpleRobertaTokenize(text: text, vocab: vocabMap)

            // 3. Add Special Tokens [CLS] ... [SEP]
            var specialTokensList = [Int64]()
            
            specialTokensList.append(Self.CLS_ID)
            specialTokensList.append(contentsOf: rawTokenIds)
            specialTokensList.append(Self.SEP_ID)
  
            // 4. Pad/Truncate and Return
            return padAndTruncate(specialTokensList)
        } catch {
            print("Error parsing tokenizer JSON: \(error)")
            let dummyArray = [Int64](repeating: 0, count: Self.MODEL_FIXED_LENGTH)
            return TokenizationResult(inputIds: dummyArray, attentionMask: dummyArray)
        }
    }
}

enum MyInferenceError: LocalizedError {
    case fileNotFound(String)
    case tensorConversionFailed
    
    // Provide a human-readable description for printing/debugging
    var errorDescription: String? {
        switch self {
        case .fileNotFound(let path):
            return "File not found locally at: \(path)"
        case .tensorConversionFailed:
            return "Failed to convert model output to a readable tensor."
        }
    }
}

// Array Extension for safe access (Equivalent to Kotlin's getOrNull)
extension Array {
    func getOrNil(_ index: Int) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}
