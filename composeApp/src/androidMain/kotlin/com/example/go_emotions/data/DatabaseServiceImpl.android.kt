package com.example.go_emotions.data

import android.util.Log
import com.example.go_emotions.domain.DatabaseService
import com.example.go_emotions.domain.safecall.safeFirebaseCall
//import com.google.firebase.Firebase
//import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.storage
//import kotlinx.coroutines.tasks.await
//import com.google.firebase.FirebaseApp
//import java.io.File
import com.example.go_emotions.domain.errorhandling.DataError
import com.example.go_emotions.domain.errorhandling.Result

actual class DatabaseServiceImpl actual constructor() : DatabaseService {
//    private val db: FirebaseStorage = Firebase.storage
//
//    private val context = FirebaseApp.getInstance().applicationContext

    actual override suspend fun getTokenizer(): Result<Boolean, DataError> {
        return safeFirebaseCall {
//            val fileName = "tokenizer.json"
//            val internalFile = File(context.filesDir, fileName)
//
//            if (!internalFile.exists()) {
//                val storageRef = db.reference.child("model/$fileName")
//                val downloadTask = storageRef.getFile(internalFile)
//
//                // Add a progress listener
//                downloadTask.addOnProgressListener { taskSnapshot ->
//                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
//                    Log.d("DownloadTracker", "Tokenizer Download is $progress% done")
//                }
//
//                // Wait for completion
//                downloadTask.await()
//            } else {
//                Log.d("DownloadTracker", "Token already Downloaded")
//            }

            true // Return true indicating the file is ready
        }
    }

    actual override suspend fun getModel(): Result<Boolean, DataError> {
        return safeFirebaseCall {
//            val fileName = "go_emotions.pte"
//            val internalFile = File(context.filesDir, fileName)
//
//            if (!internalFile.exists()) {
//                val storageRef = db.reference.child("model/$fileName")
//                val downloadTask = storageRef.getFile(internalFile)
//
//                // Add a progress listener
//                downloadTask.addOnProgressListener { taskSnapshot ->
//                    val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
//                    Log.d("DownloadTracker", "Model Download is $progress% done")
//                }
//
//                // Wait for completion
//                downloadTask.await()
//            } else {
//                Log.d("DownloadTracker", "Model already Downloaded")
//            }

            true // Return true indicating the file is ready
        }
    }
}