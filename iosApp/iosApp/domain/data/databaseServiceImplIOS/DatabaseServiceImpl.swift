//
//  DatabaseServiceImpl.swift
//  iosApp
//
//  Created by Joshua Townsend on 3/17/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import ComposeApp
//import FirebaseAuth
//import FirebaseStorage

class DatabaseServiceIOS: ComposeApp.DatabaseService {
    
    static let shared = DatabaseServiceIOS()
    typealias SuccessData = AnyObject
    typealias ErrorType = ComposeApp.DataErrorNetwork

//    let auth = Auth.auth()
//    let storage = FirebaseStorage.Storage.storage()

    func getTokenizer() async throws -> ComposeApp.Result {
        return await downloadFile(fileName: "tokenizer.json", modelPath: "model/tokenizer.json", fileType: "Tokenizer")
    }

    func getModel() async throws -> ComposeApp.Result {
        return await downloadFile(fileName: "go_emotions_coreml.pte", modelPath: "model/go_emotions_coreml.pte", fileType: "Model")
    }

    private func downloadFile(fileName: String, modelPath: String, fileType: String) async -> ComposeApp.Result {
//        // 1. Get the URL for the app's internal Documents directory
//        guard let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
//            print("DownloadTracker: ERROR - Could not access documents directory")
//            return ComposeApp.ResultError<SuccessData, ErrorType>(error: .requestFailed)
//        }
//
//        let internalFileURL = documentsDirectory.appendingPathComponent(fileName)
//
//        // 2. Check if the file already exists locally
//        if FileManager.default.fileExists(atPath: internalFileURL.path) {
//            print("DownloadTracker: \(fileType) already exists at \(internalFileURL.path)")
//            return ComposeApp.ResultSuccess<SuccessData, ErrorType>(data: true as SuccessData)
//        }
//
//        print("DownloadTracker: \(fileType) not found locally. Starting download...")
//        print("DownloadTracker: Firebase path: \(modelPath)")
//        print("DownloadTracker: Local destination: \(internalFileURL.path)")

        do {
//            // 3. Download the file from Firebase Storage
//            let storageRef = self.storage.reference()
//            let fileRef = storageRef.child(modelPath)
//
//            // Use the writeAsync extension to download the file
//            let downloadedURL = try await fileRef.writeAsync(toFile: internalFileURL, fileType: fileType)
//
//            // Verify the file was actually written
//            if FileManager.default.fileExists(atPath: downloadedURL.path) {
//                let attributes = try? FileManager.default.attributesOfItem(atPath: downloadedURL.path)
//                let fileSize = attributes?[.size] as? Int64 ?? 0
//                print("DownloadTracker: \(fileType) download complete. File size: \(fileSize) bytes")
//                print("DownloadTracker: Saved at \(downloadedURL.path)")
//                return ComposeApp.ResultSuccess<SuccessData, ErrorType>(data: true as SuccessData)
//            } else {
//                print("DownloadTracker: ERROR - File does not exist after download")
//                return ComposeApp.ResultError<SuccessData, ErrorType>(error: .requestFailed)
//            }
//            
            return ComposeApp.ResultSuccess<SuccessData, ErrorType>(data: true as SuccessData)

        } catch {
            // 4. Handle and Map Errors with detailed logging
            print("DownloadTracker: ERROR - \(fileType) download failed")
            print("DownloadTracker: Error description: \(error.localizedDescription)")

            let nsError = error as NSError
            print("DownloadTracker: Error domain: \(nsError.domain)")
            print("DownloadTracker: Error code: \(nsError.code)")
            print("DownloadTracker: Error info: \(nsError.userInfo)")

            // Check for specific Firebase Storage errors
//            if let errorCode = StorageErrorCode(rawValue: nsError.code) {
//                switch errorCode {
//                case .objectNotFound:
//                    print("DownloadTracker: File not found in Firebase Storage at path: \(modelPath)")
//                case .unauthorized:
//                    print("DownloadTracker: Unauthorized - Check Firebase Storage rules")
//                case .unauthenticated:
//                    print("DownloadTracker: Not authenticated - User may need to sign in")
//                case .downloadSizeExceeded:
//                    print("DownloadTracker: Download size exceeded limit")
//                default:
//                    print("DownloadTracker: Firebase Storage error: \(errorCode)")
//                }
//            }

            if nsError.code == NSURLErrorNotConnectedToInternet {
                return ComposeApp.ResultError<SuccessData, ErrorType>(error: ComposeApp.DataErrorNetwork.noInternetConnection)
            } else if nsError.code == NSURLErrorTimedOut {
                return ComposeApp.ResultError<SuccessData, ErrorType>(error: ComposeApp.DataErrorNetwork.requestTimeout)
            } else {
                return ComposeApp.ResultError<SuccessData, ErrorType>(error: ComposeApp.DataErrorNetwork.requestFailed)
            }
        }
    }
}


// Extension to wrap Firebase Storage's callback method into an async function
//extension StorageReference {
//    func writeAsync(toFile localURL: URL, fileType: String) async throws -> URL {
//        return try await withCheckedThrowingContinuation { continuation in
//            let downloadTask = self.write(toFile: localURL) { url, error in
//                if let error = error {
//                    continuation.resume(throwing: error)
//                } else if let url = url {
//                    continuation.resume(returning: url)
//                } else {
//                    continuation.resume(throwing: NSError(domain: "StorageError", code: 999, userInfo: [NSLocalizedDescriptionKey: "Unknown download error - URL is nil"]))
//                }
//            }
//
//            // Attach progress monitoring
//            downloadTask.observe(.progress) { snapshot in
//                if let progress = snapshot.progress {
//                    let percentComplete = 100.0 * Double(progress.completedUnitCount) / Double(progress.totalUnitCount)
//                    print("DownloadTracker: \(fileType) download is \(String(format: "%.1f", percentComplete))% complete (\(progress.completedUnitCount)/\(progress.totalUnitCount) bytes)")
//                }
//            }
//
//            // Monitor for failure
//            downloadTask.observe(.failure) { snapshot in
//                if let error = snapshot.error {
//                    print("DownloadTracker: \(fileType) download failed with error: \(error.localizedDescription)")
//                }
//            }
//        }
//    }
//}
