package com.example.go_emotions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
//import com.google.firebase.Firebase
//import com.google.firebase.appcheck.appCheck
//import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
//import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
//import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

//        Firebase.initialize(this)

//        if (BuildConfig.DEBUG) {
//            Firebase.appCheck.installAppCheckProviderFactory(
//                DebugAppCheckProviderFactory.getInstance()
//            )
//        } else {
//            Firebase.appCheck.installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance()
//            )
//        }

        setContent {
            App()
        }
    }
}
