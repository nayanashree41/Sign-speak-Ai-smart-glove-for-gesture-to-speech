package com.example.signova

/**
 * Offline sentence-intelligence fallback used while the project is being built.
 * Replace/extend this class with your chosen local or cloud agent when the
 * Agentic AI phase starts. The UI and data flow do not need to change.
 */
object SentenceAgent {
    fun generate(sequence: List<String>): String {
        if (sequence.isEmpty()) return "Your sentence will appear here."
        val normalized = sequence.map { it.uppercase() }
        val key = normalized.joinToString(" ")
        return when {
            "EMERGENCY" in normalized || "HELP" in normalized && "PAIN" in normalized ->
                "I need help immediately."
            key.endsWith("I NEED WATER") || key == "NEED WATER" ->
                "Could you please give me some water?"
            key.endsWith("I NEED FOOD") || key == "NEED FOOD" ->
                "Could you please give me something to eat?"
            key.contains("MEDICINE") ->
                "I need my medicine, please."
            key.contains("BATHROOM") ->
                "I need to use the washroom, please."
            key == "HELLO" -> "Hello."
            key == "THANK YOU" -> "Thank you."
            key == "PLEASE" -> "Please."
            key == "YES" -> "Yes."
            key == "NO" -> "No."
            key == "WATER" -> "I would like some water, please."
            key == "FOOD" -> "I would like something to eat, please."
            key == "HELP" -> "Please help me."
            else -> normalized.joinToString(" ")
                .lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } + "."
        }
    }
}
