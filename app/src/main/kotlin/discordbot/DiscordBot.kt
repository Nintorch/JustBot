package discordbot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.io.File

const val botPrefix = "$"

object DiscordBot : ListenerAdapter() {
    val jda = JDABuilder.createDefault(File("token.txt").readText())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(this)
            .build()

    val user get() = jda.selfUser as User

    init {
        CommandManager.registerCommands()
        jda.awaitReady()
        println("Ready")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.author == user)
            return

        if (event.message.contentRaw.startsWith(botPrefix))
            CommandManager.executeCommand(event)
        else
            CommandManager.tryNextMessageHandler(event)
    }
}

fun main() {
    DiscordBot // lazily initialize the bot
}