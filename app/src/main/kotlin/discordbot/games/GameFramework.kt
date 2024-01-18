package discordbot.games

import discordbot.Command
import discordbot.CommandManager
import discordbot.discordBot
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class TextGame : ListenerAdapter() {
    lateinit var users: List<User>
    lateinit var channel: MessageChannel

    abstract val name: String
    abstract val description: String
    abstract fun start()

    fun prepare(users: List<User>, channel: MessageChannel) {
        this.users = users
        this.channel = channel
    }
}

private val joinEmoji = Emoji.fromUnicode("\uD83C\uDF89")
private val finishEmoji = Emoji.fromUnicode("\uD83D\uDC4D")

inline fun registerGameCommand(name: String, description: String, minUserCount: Int, maxUserCount: Int,
                               crossinline factory: (sender: User, channel: MessageChannel, args: String) -> TextGame?) {
    val command = Command(name, description, "fun")
    { sender: User, channel: MessageChannel, args: String ->
        val game = factory(sender, channel, args) ?: return@Command
        GamePrepare(game, minUserCount, maxUserCount, channel)
    }
    CommandManager.registerCommand(command)
}

class GamePrepare(val game: TextGame,
                  val minUserCount: Int,
                  val maxUserCount: Int,
                  val channel: MessageChannel) : ListenerAdapter() {
    init {
        discordBot.jda.addEventListener(this)
        sendMessage()
    }

    lateinit var message: Message

    private fun sendMessage() {
        val builder = StringBuilder()
        builder.appendLine("A wild game just appeared:\n")
        builder.appendLine("**${game.name}**")
        builder.appendLine(game.description + '\n')
        if (minUserCount == maxUserCount)
            builder.appendLine("Number of players: $minUserCount")
        else
            builder.appendLine("Number of players: from $minUserCount to $maxUserCount")
        builder.appendLine("React with \"${joinEmoji.formatted}\" to enter!")

        if (minUserCount != maxUserCount)
            builder.append("React with \"${finishEmoji.formatted}\" when the user count is enough " +
                    "to start the game earlier!")

        message = channel.sendMessage(builder.toString()).complete()
        message.addReaction(joinEmoji).complete()
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.user != null && event.user == discordBot.user)
            return

        if (event.retrieveMessage().complete() != message)
            return

        val users = message.retrieveReactionUsers(joinEmoji).complete()
            .filter { it != discordBot.user && !it.isBot }.take(maxUserCount)

        when (event.reaction.emoji.asUnicode()) {
            joinEmoji -> {
                if (users.size == maxUserCount)
                    startGame(users, false)
            }
            finishEmoji -> {
                if (users.size >= minUserCount)
                    startGame(users, true)
            }
        }
    }

    private fun startGame(users: List<User>, early: Boolean) {
        channel.sendMessage((if (early) "The game was requested to start earlier. " else "") +
            "May the game begin! Players: ${users.joinToString(", ") { it.effectiveName }}").complete()
        discordBot.jda.removeEventListener(this)
        game.prepare(users, channel)
        game.start()
    }
}