package discordbot.games

import discordbot.CommandManager
import discordbot.dms
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import java.net.URL
import kotlin.random.Random

// TODO: a mode where the explainer can decide if the guesser has guessed the word correctly
class Crocodile(private val difficulty: Difficulty) : TextGame() {
    companion object {
        val command = wrapGameCommand("crocodile", "Игра в крокодила", 2, 2)
        { _: User, channel: MessageChannel, args: String ->
            val difficulty = when (args.lowercase()) {
                "easy" -> Crocodile.Companion.Difficulty.EASY
                "medium" -> Crocodile.Companion.Difficulty.MEDIUM
                else -> {
                    channel.sendMessage("В качестве сложности введите easy или medium.").complete()
                    return@wrapGameCommand null
                }
            }
            Crocodile(difficulty)
        }

        enum class Difficulty {
            EASY,
            MEDIUM,
        }

        private var easyWords: List<String>
        private var mediumWords: List<String>

        init {
            val urlText = URL("https://slotobzor.com/populyarnye/interesnye-slova-dlya-igry-krokodil/").readText()

            var index1 = urlText.indexOf("<ol>")
            var index2 = urlText.indexOf("</ol>")
            easyWords = urlText.substring(index1+5, index2-1).split('\n')
                .map { it.drop(4).dropLast(5) }

            index1 = urlText.indexOf("<ol>", index2)
            index2 = urlText.indexOf("</ol>", index1)
            mediumWords = urlText.substring(index1+5, index2-1).split('\n')
                .map { it.drop(4).dropLast(5) }
        }
    }

    override val name get() = "Крокодил"
    override val description get() = "Один человек должен объяснить слово, " +
            "а другой должен его угадать"

    private lateinit var explainer: User
    private lateinit var secondUser: User

    private var currentWord = ""

    override fun start() {
        val value = Random.nextInt(2)
        explainer = users[value]
        secondUser = users[1-value]

        val builder = StringBuilder()
        builder.appendLine("В этот раз слова объясняет ${explainer.effectiveName}.")
        builder.appendLine("Объясняющему было отправлено слово, " +
                "теперь угадывающий должен его отгадать, отправляя слова в этом канале.")
        builder.append("Угадывающий должен отправить \"пропустить\", если он не может угадать слово, ")
        builder.append("или \"закончить\", чтобы закончить игру.")
        channel.sendMessage(builder.toString()).complete()
        nextWord()
    }

    private fun nextWord() {
        currentWord = (if (difficulty == Difficulty.EASY) easyWords else mediumWords).random()
        explainer.dms.sendMessage("Ваше следующее слово: $currentWord").complete()
        listenToSecondUser()
    }

    private fun listenToSecondUser() = CommandManager.registerNextMessageHandler(secondUser, channel, this::listener)
    private fun stopListening() = CommandManager.resetNextMessageHandler(secondUser, channel)

    private fun listener(sender: User, channel: MessageChannel, args: String) {
        when (args.lowercase()) {
            currentWord -> {
                channel.sendMessage("Молодец, ${secondUser.effectiveName}! Отправляю следующее слово.").complete()
                nextWord()
            }
            "пропустить" -> {
                channel.sendMessage("Хорошо, пропускаем. Отправляю следующее слово.").complete()
                nextWord()
            }
            "закончить" -> {
                channel.sendMessage("Игра закончена!").complete()
                stopListening()
            }
            else -> listenToSecondUser()
        }
    }
}