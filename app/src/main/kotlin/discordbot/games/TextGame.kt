package discordbot.games

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class TextGame : ListenerAdapter() {
    abstract val name: String
    abstract val description: String
    abstract fun start(users: List<User>)
}