package com.rescuedesk.ai.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import com.rescuedesk.ai.app.ServiceLocator
import com.rescuedesk.ai.ui.navigation.RescueDeskApp
import com.rescuedesk.ai.ui.theme.RescueDeskTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RescueDeskTheme {
                // Unload any loaded model when the last screen goes away (PRD arch §5.3).
                DisposableEffect(Unit) {
                    onDispose { ServiceLocator.aiEngine.unload() }
                }
                RescueDeskApp()
            }
        }
    }
}
