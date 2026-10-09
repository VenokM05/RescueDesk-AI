package com.rescuedesk.ai.data.search

/**
 * Lightweight, conservative morphology helper for Filipino/Taglish queries
 * (PRD section 5.9 "handle simple spelling mistakes / support Filipino"; roadmap
 * Phase 3 NO-GO "polish search morphology"). Not a full stemmer — it only
 * widens recall so a conjugated verb (e.g. "bumaha", "maglilindol",
 * "umuuulan") still reaches the root that appears in guide text ("baha",
 * "lindol", "ulan").
 *
 * Design rules:
 *  - the exact original token is always the FIRST candidate, so we never lose
 *    precision; extra forms only append OR-matches (recall), which is safe for
 *    a small guide corpus;
 *  - only emit a stripped root when it is still substantial (>= 4 chars), so
 *    short English tokens are barely affected;
 *  - cap the candidate set so a long query can't build a huge MATCH string.
 */
object FilipinoQueryExpander {

    private val prefixes = listOf(
        "magpa", "mag", "nag", "nang", "pang", "pa", "ka", "ma", "na",
        "um", "in", "ping", "ia", "ika", "taga"
    )
    private val suffixes = listOf("an", "in", "on", "han")

    fun candidates(token: String): List<String> {
        val t = token.lowercase().trim()
        if (t.length < 4) return listOf(t)

        val out = LinkedHashSet<String>()
        out.add(t)

        // 1) Prefix stripping (+ de-reduplication of the remainder).
        for (p in prefixes) {
            if (t.startsWith(p) && t.length - p.length >= 4) {
                val root = t.removePrefix(p)
                out.add(root)
                deReduplicate(root)?.let { out.add(it) }
                break
            }
        }

        // 2) Infix "-um-"/"-in-" after the first syllable: b-um-aha -> baha.
        Regex("^(\\D{1,3})(um|in)(.+)").find(t)?.let { m ->
            val root = m.groupValues[1] + m.groupValues[3]
            if (root.length >= 4) out.add(root)
        }

        // 3) Suffix stripping: iligtas -> ... , "lindol" + "an" etc.
        for (s in suffixes) {
            if (t.endsWith(s) && t.length - s.length >= 4) {
                out.add(t.removeSuffix(s))
                break
            }
        }

        return out.filter { it.length >= 3 }.take(MAX_CANDIDATES).toList()
    }

    /** "lilindol" -> "lindol" when the leading two-char syllable repeats. */
    private fun deReduplicate(word: String): String? =
        if (word.length >= 5 && word.substring(0, 2) == word.substring(2, 4))
            word.substring(2)
        else null

    private const val MAX_CANDIDATES = 4
}
