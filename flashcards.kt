package flashcards

import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

fun main(args: Array<String>) {
    val game = FlashCardsGame(args)
    game.mainMenu()
}

class FlashCardsGame(private val args: Array<String>) {
    private val flashCards = mutableMapOf<String, String>()
    private val mistakesAmount = mutableMapOf<String, Int>()
    private val log = mutableListOf<String>()
    init {
        for (i in args.indices) {
            if (args[i] == "-import") {
                val importFileName = File(args[i + 1])
                var addedCards = 0
                val importList = mutableListOf<String>()
                importFileName.forEachLine { importList.add(it) }

                for (i in importList) {
                    val (term, definition, mistakes) = i.split(" : ")
                    if (!flashCards.containsKey(term)) {
                        addedCards ++
                        mistakesAmount[term] = mistakes.toInt()
                    } else {
                        mistakesAmount[term] = mistakesAmount[term]!!.plus(mistakes.toInt())
                    }
                    flashCards[term] = definition
                }
                output("$addedCards cards have been loaded.\n")
            }
        }
    }

    fun mainMenu() {
        while (true) {
            output("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            when (input()) {
                "add" -> addCard()
                "remove" -> removeCard()
                "import" -> importFromFile()
                "export" -> exportIntoFile()
                "ask" -> guessCorrectDefinition()
                "log" -> saveLogIntoFile()
                "hardest card" -> findHardestCard()
                "reset stats" -> resetStats()
                "exit" -> {
                    output("Bye bye!\n")
                    exitFromGame()
                    return
                }
            }
        }
    }

    private fun addCard() {

        output("The card:")
        val term = readln()
        if (flashCards.containsKey(term)) {
            output("The card \"$term\" already exists.\n")
            return
        }

        output("The definition of the card:")
        val definition: String = readln()
        if (flashCards.containsValue(definition)) {
            output("The definition \"$definition\" already exists.\n")
            return
        }

        flashCards[term] = definition
        mistakesAmount[term] = 0
        output("The pair (\"$term\":\"$definition\") has been added.\n")
    }

    private fun removeCard() {
        output("Which card?")
        val keyForRemove = readln()

        if (flashCards.containsKey(keyForRemove)) {
            flashCards.remove(keyForRemove)
            output("The card has been removed.\n")
        } else {
            output("Can't remove \"$keyForRemove\": there is no such card.\n")
        }
    }

    private fun guessCorrectDefinition() {
        output("How many times to ask?")
        val askingRepeat = input().toInt()

        for (i in 1..askingRepeat) {
            val randomCardKey = flashCards.keys.elementAt(Random.nextInt(flashCards.size))
            output("Print the definition of \"${randomCardKey}\":")
            val answer = input()

            when {
                answer == flashCards[randomCardKey] -> {
                    output("Correct!\n")
                }
                flashCards.containsValue(answer) -> {
                    mistakesAmount[randomCardKey] = mistakesAmount[randomCardKey]!!.plus(1)
                    output("Wrong. The right answer is \"${flashCards[randomCardKey]}\", " +
                            "but your definition is correct for \"${searchTermByDefinition(flashCards, answer)}\".\n")
                }
                else -> {
                    mistakesAmount[randomCardKey] = mistakesAmount[randomCardKey]!!.plus(1)
                    output("Wrong. The right answer is \"${flashCards[randomCardKey]}\".\n")
                }
            }
        }
    }

    private fun searchTermByDefinition(flashCards: MutableMap<String, String>, definition: String): String {
        for ((key, value) in flashCards) {
            if (value == definition) return key
        }
        return "I didn't find that.\n"
    }

    private fun importFromFile() {
        output("File name:")
        val fileName = File(input())

        if (fileName.exists()) {
            var addedCards = 0
            val importList = mutableListOf<String>()
            fileName.forEachLine { importList.add(it) }

            for (i in importList) {
                val (term, definition, mistakes) = i.split(" : ")
                if (!flashCards.containsKey(term)) {
                    addedCards ++
                    flashCards[term] = definition
                    mistakesAmount[term] = mistakes.toInt()
                } else {
                    flashCards[term] = definition
                    mistakesAmount[term] = mistakesAmount[term]!!.plus(mistakes.toInt())
                }
            }
            output("$addedCards cards have been loaded.\n")
        } else output("File not found.\n")
    }

    private fun exportIntoFile() {
        output("File name:")
        val fileName = File(readln())
        fileName.createNewFile()

        for ((key, value) in flashCards) {
            fileName.appendText("$key : $value : ${mistakesAmount[key]}\n")
        }

        output("${flashCards.size} cards have been saved.\n")
    }

    private fun findHardestCard() {
        if (mistakesAmount.isEmpty()) {
            output("There are no cards with errors.\n")
        } else {
            var maxMistakes = 0

            for ((_, value) in mistakesAmount) {
                if (value > maxMistakes) maxMistakes = value
            }

            val hardestCards = mistakesAmount.filterKeys { mistakesAmount[it] == maxMistakes }.map { it.key }.toList()
            if (hardestCards.size == 1) {
                output("The hardest card is \"${hardestCards[0]}\". You have $maxMistakes errors answering it.\n")
            } else {
                output("The hardest cards are \"${hardestCards.joinToString("\", \"")}\". You have $maxMistakes errors answering them.\n")
            }
        }
    }

    private fun resetStats() {
        mistakesAmount.clear()
        output("Card statistics have been reset.\n")
    }

    private fun saveLogIntoFile() {
        output("File name:")
        val logFileName = File(input())
        logFileName.createNewFile()
        output("The log has been saved.\n")
        logFileName.writeText(log.joinToString("\n"))
        logFileName.forEachLine { println(it) }
    }

    fun exitFromGame() {
        for (i in args.indices) {
            if (args[i] == "-export") {
                val fileName = File(args[i + 1])
                fileName.createNewFile()

                for ((key, value) in flashCards) {
                    fileName.appendText("$key : $value : ${mistakesAmount[key]}\n")
                }

                output("${flashCards.size} cards have been saved.\n")
            }
        }
    }

    private fun output(string: String) {
        println(string)
        log.add(string)
    }

    private fun input(): String {
        val input = readln()
        log.add(input)
        return input
    }
}
