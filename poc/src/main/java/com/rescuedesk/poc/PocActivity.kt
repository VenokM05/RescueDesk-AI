package com.rescuedesk.poc

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rescuedesk.poc.engine.LiteRtEngine
import com.rescuedesk.poc.engine.LlamaCppEngine
import com.rescuedesk.poc.engine.PromptedEngine
import com.rescuedesk.poc.run.ResourceSampler
import com.rescuedesk.poc.run.Runner
import kotlinx.coroutines.launch
import java.io.File

enum class EngineChoice { LITERT, LLAMA_CPP }

class PocViewModel(application: Application) : AndroidViewModel(application) {

    var choice by mutableStateOf(EngineChoice.LITERT)
    var busy by mutableStateOf(false)
    var log by mutableStateOf("Idle. Push a model file into files/models/ first (see README in docs/PHASE1-MODEL-POC.md section 5.1).")

    // App-private files dir — target of `adb push ... files/models/` (section 5.1).
    // Created on access so adb push never fails on a missing directory.
    val modelDir: File
        get() = File(getApplication<Application>().filesDir, "models").apply { mkdirs() }

    fun runBank() {
        if (busy) return
        busy = true
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val engine: PromptedEngine = when (choice) {
                EngineChoice.LITERT -> LiteRtEngine(File(modelDir, "gemma3_1b-it-int4.task"))
                EngineChoice.LLAMA_CPP -> LlamaCppEngine(File(modelDir, "qwen3-0.6b-q4_k_m.gguf"))
            }
            log += "\n[load] ${engine.status.value}"
            engine.ensureLoaded().onFailure { e ->
                log += "\n[load failed] ${e.message}"
                log += "\n[harness OK] adapters + runner are wired; bind the native runtime per the adapter's checklist."
            }
            val sampler = ResourceSampler(ctx)
            val runner = Runner(engine, sampler, choice.name.lowercase())
            val out = runner.runAll(ctx.assets, File(ctx.filesDir, "results"))
            engine.unload()
            log += "\n[jsonl] ${out.absolutePath}"
            busy = false
        }
    }
}

@Composable
fun PocScreen(vm: PocViewModel = viewModel()) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("RescueDesk PoC harness", style = MaterialTheme.typography.headlineSmall)
            Text("Phase 1 Go/No-Go evidence runner — spike module, never ships with :app.")

            Row(verticalAlignment = Alignment.CenterVertically) {
                EngineChoice.entries.forEach { c ->
                    Row(
                        Modifier.selectable(
                            selected = vm.choice == c,
                            onClick = { vm.choice = c }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = vm.choice == c, onClick = { vm.choice = c })
                        Text(when (c) {
                            EngineChoice.LITERT -> "LiteRT-LM"
                            EngineChoice.LLAMA_CPP -> "llama.cpp"
                        })
                    }
                }
            }

            Button(
                onClick = { vm.runBank() },
                enabled = !vm.busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (vm.busy) "Running bank…" else "Run 24-question bank") }

            OutlinedButton(
                onClick = { vm.log += "\n[models dir] ${vm.modelDir.absolutePath}\nadb push <artifact> ${vm.modelDir.absolutePath}/" },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Show model push path") }

            if (vm.busy) CircularProgressIndicator(Modifier.padding(8.dp))
            Text(vm.log, style = MaterialTheme.typography.bodySmall)
        }
    }
}

class PocActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { PocScreen() }
        }
    }
}
