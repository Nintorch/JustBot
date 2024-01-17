package discordbot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.io.File

const val botPrefix = "$"

class DiscordBot : ListenerAdapter() {
    val jda = JDABuilder.createDefault(File("token.txt").readText())
            .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
            .addEventListeners(this)
            .setActivity(Activity.playing("some game"))
            .build()
    inline val user: User get() = jda.selfUser

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

val discordBot by lazy { DiscordBot() }

fun main() {
    discordBot // lazily initialize the property
}