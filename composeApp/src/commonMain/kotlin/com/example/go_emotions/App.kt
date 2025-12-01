package com.example.go_emotions

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {

        val mainViewmodel: MainViewModel = koinViewModel()
        val state = mainViewmodel.state.collectAsStateWithLifecycle().value
        val onEvent: (MainScreenEvent) -> Unit = mainViewmodel::onEvent

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val input = remember { mutableStateOf("") }

            Text(
                text = "Emotions Prediction",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.heightIn(50.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    label = { Text(text = "How are you feeling today?") },
                    value = input.value,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = false, // Changed to false so text wraps                    maxLines = 5,
                    enabled = !state.showProgressionBar,
                    onValueChange = {
                        if (it.length <= 512) {
                            input.value = it
                        }
                    },
                    supportingText = {
                        Text(
                            text = "${input.value.length} / 512",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = MaterialTheme.shapes.medium),
                )

                Spacer(modifier = Modifier.heightIn(16.dp))

                Crossfade(
                    targetState = state.showProgressionBar
                ) { show ->
                    if(!show) {
                        Text (
                            text = state.predictionText,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                Spacer(modifier = Modifier.heightIn(16.dp))

                Button(
                    onClick = {
                        onEvent(MainScreenEvent.GetPrediction)
                        onEvent(MainScreenEvent.InputField(input.value))
                        onEvent(MainScreenEvent.ShowProgression(true))
                    }) {
                    Text("Predict")
                }

                Spacer(modifier = Modifier.heightIn(16.dp))

                Button(
                    onClick = {
                        onEvent(MainScreenEvent.ClearOutput)
                    }) {
                    Text("Clear Output")
                }
            }
        }
    }
}