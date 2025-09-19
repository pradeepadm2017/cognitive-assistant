package com.cognitiveassistant.utils

import java.text.SimpleDateFormat
import java.util.*

object AnswerVerifier {

    fun verifyAnswer(questionId: String, spokenAnswer: String): Boolean {
        val answer = spokenAnswer.lowercase().trim()

        return when (questionId) {
            // ORIENTATION TO TIME
            "orientation_year" -> verifyYear(answer)
            "orientation_season" -> verifySeason(answer)
            "orientation_date" -> verifyDate(answer)
            "orientation_day" -> verifyDay(answer)
            "orientation_month" -> verifyMonth(answer)

            // ORIENTATION TO PLACE (These would need to be configured based on actual location)
            "orientation_state" -> verifyState(answer)
            "orientation_county" -> verifyCounty(answer)
            "orientation_town" -> verifyTown(answer)
            "orientation_hospital" -> verifyHospital(answer)
            "orientation_floor" -> verifyFloor(answer)

            // REGISTRATION - Check if they can repeat the words
            "registration_1" -> answer.contains("apple")
            "registration_2" -> answer.contains("penny")
            "registration_3" -> answer.contains("table")

            // ATTENTION/CALCULATION - Serial 7s
            "attention_1" -> verifySerialSeven(answer, 93)
            "attention_2" -> verifySerialSeven(answer, 86)
            "attention_3" -> verifySerialSeven(answer, 79)
            "attention_4" -> verifySerialSeven(answer, 72)
            "attention_5" -> verifySerialSeven(answer, 65)

            // RECALL - Remember the three words
            "recall_1" -> answer.contains("apple")
            "recall_2" -> answer.contains("penny")
            "recall_3" -> answer.contains("table")

            // LANGUAGE
            "language_naming_1" -> verifyWatchNaming(answer)
            "language_naming_2" -> verifyPencilNaming(answer)
            "language_repetition" -> verifyRepetition(answer)
            "language_comprehension_1", "language_comprehension_2", "language_comprehension_3" -> true // Physical actions - assume correct for demo
            "language_reading" -> true // Physical action - assume correct for demo
            "language_writing" -> verifySentence(answer)
            "language_copying" -> true // Physical action - assume correct for demo

            else -> false
        }
    }

    private fun verifyYear(answer: String): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return answer.contains(currentYear.toString()) || answer.contains("$currentYear")
    }

    private fun verifySeason(answer: String): Boolean {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val expectedSeason = when (currentMonth) {
            11, 0, 1 -> "winter"
            2, 3, 4 -> "spring"
            5, 6, 7 -> "summer"
            8, 9, 10 -> "fall"
            else -> "fall"
        }
        return answer.contains(expectedSeason) || answer.contains("autumn")
    }

    private fun verifyDate(answer: String): Boolean {
        val currentDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        return answer.contains(currentDate.toString()) || containsDateNumbers(answer, currentDate)
    }

    private fun verifyDay(answer: String): Boolean {
        val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()).lowercase()
        return answer.contains(currentDay) || answer.contains(currentDay.substring(0, 3))
    }

    private fun verifyMonth(answer: String): Boolean {
        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()).lowercase()
        val shortMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(Date()).lowercase()
        return answer.contains(currentMonth) || answer.contains(shortMonth)
    }

    private fun verifyState(answer: String): Boolean {
        // For demo purposes, accept common state mentions
        // In real implementation, this would be configured based on actual location
        val commonStates = listOf("california", "texas", "florida", "new york", "illinois", "pennsylvania")
        return commonStates.any { answer.contains(it) } || answer.contains("state")
    }

    private fun verifyCounty(answer: String): Boolean {
        // For demo purposes, accept county mentions
        return answer.contains("county") || answer.contains("parish") || answer.contains("district")
    }

    private fun verifyTown(answer: String): Boolean {
        // For demo purposes, accept city mentions
        val commonCities = listOf("los angeles", "new york", "chicago", "houston", "phoenix", "philadelphia")
        return commonCities.any { answer.contains(it) } || answer.contains("city") || answer.contains("town")
    }

    private fun verifyHospital(answer: String): Boolean {
        // For demo purposes, accept hospital/clinic mentions
        return answer.contains("hospital") || answer.contains("clinic") || answer.contains("medical") || answer.contains("center")
    }

    private fun verifyFloor(answer: String): Boolean {
        // Accept any floor number
        return answer.contains("floor") || containsNumber(answer) || answer.contains("ground") || answer.contains("first") || answer.contains("second")
    }

    private fun verifySerialSeven(answer: String, expectedValue: Int): Boolean {
        return answer.contains(expectedValue.toString()) || extractNumber(answer) == expectedValue
    }

    private fun verifyWatchNaming(answer: String): Boolean {
        return answer.contains("watch") || answer.contains("clock") || answer.contains("timepiece")
    }

    private fun verifyPencilNaming(answer: String): Boolean {
        return answer.contains("pencil") || answer.contains("pen") || answer.contains("writing")
    }

    private fun verifyRepetition(answer: String): Boolean {
        val targetPhrase = "no ifs ands or buts"
        val cleanAnswer = answer.replace(",", "").replace(".", "")
        return cleanAnswer.contains(targetPhrase) ||
               (answer.contains("no") && answer.contains("ifs") && answer.contains("ands") && answer.contains("buts"))
    }

    private fun verifySentence(answer: String): Boolean {
        // Basic sentence verification - has some words and structure
        val words = answer.split(" ").filter { it.isNotBlank() }
        return words.size >= 3 && (answer.contains(".") || answer.contains("!") || answer.contains("?") || words.size >= 4)
    }

    private fun containsDateNumbers(answer: String, expectedDate: Int): Boolean {
        val numbers = extractNumbers(answer)
        return numbers.contains(expectedDate)
    }

    private fun containsNumber(answer: String): Boolean {
        return answer.any { it.isDigit() } ||
               answer.contains("one") || answer.contains("two") || answer.contains("three") ||
               answer.contains("four") || answer.contains("five") || answer.contains("six") ||
               answer.contains("seven") || answer.contains("eight") || answer.contains("nine") ||
               answer.contains("ten") || answer.contains("first") || answer.contains("second") ||
               answer.contains("third") || answer.contains("ground")
    }

    private fun extractNumber(answer: String): Int? {
        val numberPattern = Regex("\\d+")
        val match = numberPattern.find(answer)
        return match?.value?.toIntOrNull()
    }

    private fun extractNumbers(answer: String): List<Int> {
        val numberPattern = Regex("\\d+")
        return numberPattern.findAll(answer).mapNotNull { it.value.toIntOrNull() }.toList()
    }

    fun getQuestionText(questionId: String): String {
        return when (questionId) {
            "orientation_year" -> "What year is it?"
            "orientation_season" -> "What season is it?"
            "orientation_date" -> "What is today's date?"
            "orientation_day" -> "What day of the week is it?"
            "orientation_month" -> "What month is it?"
            "orientation_state" -> "What state are we in?"
            "orientation_county" -> "What county or region are we in?"
            "orientation_town" -> "What city or town are we in?"
            "orientation_hospital" -> "What is the name of this building or hospital?"
            "orientation_floor" -> "What floor are we on?"
            "registration_1" -> "I will say three words. Please repeat the first word: Apple"
            "registration_2" -> "Please repeat the second word: Penny"
            "registration_3" -> "Please repeat the third word: Table"
            "attention_1" -> "Now subtract 7 from 100. What is 100 minus 7?"
            "attention_2" -> "Continue subtracting 7. What is 93 minus 7?"
            "attention_3" -> "What is 86 minus 7?"
            "attention_4" -> "What is 79 minus 7?"
            "attention_5" -> "What is 72 minus 7?"
            "recall_1" -> "Now, what was the first word I asked you to remember?"
            "recall_2" -> "What was the second word?"
            "recall_3" -> "What was the third word?"
            "language_naming_1" -> "What is this object called? A watch or clock."
            "language_naming_2" -> "What is this object called? A pencil or pen."
            "language_repetition" -> "Please repeat this phrase exactly: No ifs, ands, or buts"
            "language_comprehension_1" -> "Please follow this instruction: Take an imaginary paper in your right hand"
            "language_comprehension_2" -> "Fold that imaginary paper in half"
            "language_comprehension_3" -> "Put the paper on the floor"
            "language_reading" -> "Read this instruction and follow it: Close your eyes for 3 seconds, then open them"
            "language_writing" -> "Please say a complete sentence - any sentence with a subject and verb"
            "language_copying" -> "Imagine you are copying a drawing of two intersecting pentagons. Say 'completed' when you're done"
            else -> "Question not found"
        }
    }
}