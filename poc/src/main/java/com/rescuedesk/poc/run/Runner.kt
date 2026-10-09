package com.rescuedesk.poc.run

import com.rescuedesk.poc.engine.AiEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class PocQuestion(
    val id: Int,
    val category: String,
    val lang: String,
    val text: String,
    val expectedBehavior: String,
    val chunks: List<String>
)

/**
 * Harness piece §5.2: replays the question bank through the engine and dumps
 * JSONL rows {question, retrieved_chunks, raw_answer, validated_citations,
 * tok_per_s, ttfb_ms, rss_kb, thermal} for the §7 results sheet and dual-rater
 * scoring. The app's real FTS retrieval replaces the asset chunks before
 * Week-2 measurement; until then chunks come from questions.txt (§6 note).
 */
class Runner(
    private val engine: AiEngine,
    private val sampler: ResourceSampler,
    private val engineName: String
) {

    fun loadQuestions(assets: android.content.res.AssetManager): List<PocQuestion> =
        assets.open("questions.txt").bufferedReader().use { reader ->
            reader.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .mapNotNull { line ->
                    val p = line.split("|")
                    if (p.size < 6) return@mapNotNull null
                    PocQuestion(
                        id = p[0].trim().toIntOrNull() ?: return@mapNotNull null,
                        category = p[1].trim(),
                        lang = p[2].trim(),
                        text = p[3].trim(),
                        expectedBehavior = p[4].trim(),
                        chunks = p[5].split("~~").map { it.trim() }.filter { it.isNotEmpty() }
                    )
                }
                .toList()
        }

    /** One full pass; appends JSONL and returns the output file. */
    suspend fun runAll(assets: android.content.res.AssetManager, outputDir: File): File =
        withContext(Dispatchers.IO) {
        val stamp = System.currentTimeMillis()
        val out = File(outputDir, "poc-$engineName-$stamp.jsonl")
        outputDir.mkdirs()
        out.bufferedWriter().use { writer ->
            val questions = loadQuestions(assets)
            sampler.captureBaseline()

            for (q in questions) {
                val started = System.nanoTime()
                var tokens = 0
                var ttfbMs = -1L
                val answerBuilder = StringBuilder()

                val result = engine.generate(q.text, q.chunks) { token ->
                    if (tokens == 0) ttfbMs = (System.nanoTime() - started) / 1_000_000
                    tokens++
                    answerBuilder.append(token)
                }

                val elapsedSec = ((System.nanoTime() - started) / 1_000_000.0) / 1000.0
                val row = buildString {
                    append("{")
                    append("\"engine\":").append(quote(engineName))
                    append(",\"id\":").append(q.id)
                    append(",\"category\":").append(quote(q.category))
                    append(",\"lang\":").append(quote(q.lang))
                    append(",\"question\":").append(quote(q.text))
                    append(",\"expected_behavior\":").append(quote(q.expectedBehavior))
                    append(",\"retrieved_chunks\":").append(q.chunks.joinToString(prefix = "[", postfix = "]") { quote(it) })
                    append(",\"raw_answer\":").append(quote(answerBuilder.toString()))
                    append(",\"validated_citations\":").append(citationJson(q.chunks.size, answerBuilder.toString()))
                    append(",\"error\":").append(quote(result.exceptionOrNull()?.message ?: ""))
                    append(",\"tok_per_s\":").append(if (elapsedSec > 0) String.format(Locale.US, "%.2f", tokens / elapsedSec) else "0")
                    append(",\"ttfb_ms\":").append(ttfbMs)
                    append(",\"rss_kb\":").append(sampler.rssKb())
                    append(",\"thermal\":").append(quote(sampler.thermalStatus()))
                    append("}")
                }
                writer.appendLine(row)
                writer.flush() // live-tailable over `adb logcat`/run-as even if a generation crashes
            }

            // §5.4 unload discipline: engine already unloaded per generate-loop
            // exit below; record residual so the sheet can show RSS return-to-baseline.
            engine.unload()
            val residual = sampler.waitForRelease()
            writer.appendLine("{\"unload_residual_kb\":$residual,\"baseline_rss_kb\":${sampler.baselineRssKb()}}")
        }
        out
    }

    /**
     * Citation validator (§1): every [n] marker in the answer must reference an
     * excerpt actually supplied for this question. Fabricated markers are the
     * criterion-3 hard fail, so they are recorded explicitly, not filtered.
     */
    private fun citationJson(chunkCount: Int, answer: String): String {
        val cited = Regex("\\[(\\d+)]").findAll(answer).map { it.groupValues[1].toInt() }.toSet()
        val valid = cited.filter { it in 1..chunkCount }
        val fabricated = cited - valid.toSet()
        return "{\"cited\":$cited,\"valid\":$valid,\"fabricated\":$fabricated,\"grounded\":${cited.isNotEmpty() && fabricated.isEmpty()}}"
    }

    private fun quote(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}
