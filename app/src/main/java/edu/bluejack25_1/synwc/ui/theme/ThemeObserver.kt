package edu.bluejack25_1.synwc.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import edu.bluejack25_1.synwc.data.preferences.AppPreferences
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun rememberThemeMode(): State<String> {
    val context = LocalContext.current
    val themeState = remember { mutableStateOf("system") }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(context) {
        val preferences = AppPreferences(context)
        val job = coroutineScope.launch {
            preferences.themeMode
                .distinctUntilChanged()
                .collect { mode ->
                    themeState.value = mode
                }
        }

        onDispose {
            job.cancel()
        }
    }

    return themeState
}