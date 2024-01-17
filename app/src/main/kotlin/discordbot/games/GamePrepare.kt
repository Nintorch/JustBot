package discordbot.games

import discordbot.Command
import discordbot.CommandManager
import discordbot.DiscordBot
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

private val reactionEmoji = Emoji.fromUnicode("\uD83C\uDF89")

inline fun registerGameCommand(name: String, description: String, userCount: Int, crossinline factory: () -> TextGame) {
    val command = Command(name, description, "fun")
    { _: User, channel: MessageChannel, _: String ->
        GamePrepare(factory(), userCount, channel)
    }
    CommandManager.registerCommand(command)
}

class GamePrepare(val game: TextGame, val userCount: Int, val channel: MessageChannel) : ListenerAdapter() {
    init {
        DiscordBot.bot.jda.addEventListener(this)
        sendMessage()
    }

    var message: Message? = null

    private fun sendMessage() {
        val builder = StringBuilder()
        builder.appendLine("A wild game just appeared:\n")
        builder.appendLine("**${game.name}**")
        builder.appendLine(game.description)
        builder.appendLine("Number of players: $userCount\n")
        builder.append("React with \"\uD83C\uDF89\" to enter!")

        message = channel.sendMessage(builder.toString()).complete()
        message?.addReaction(reactionEmoji)?.complete()
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.user != null && event.user == DiscordBot.bot.user)
            return

        val message = event.retrieveMessage().complete()
        if (event.retrieveMessage().complete() != message)
            return

        val users = message.retrieveReactionUsers(reactionEmoji).complete()
        users.remove(DiscordBot.bot.user)
        println(users.map { it.effectiveName })
        if (users.size >= userCount) {
            event.channel.sendMessage("May the game begin! Players: ${users.map { it.effectiveName }}").complete()
            DiscordBot.bot.jda.removeEventListener(this)
            game.start(users)
        }
    }
}