package discordbot.commands

import discordbot.Command
import discordbot.CommandManager
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import javax.sound.sampled.Clip

val HelpCommand = Command("help", "Show all commands", "main")
{ sender: User, channel: MessageChannel, _: String ->
    val builder = StringBuilder()
    builder.append("Hi, ${sender.effectiveName}! I'm JustBot and ")
    builder.appendLine("I was written in Kotlin using JDA.")
    builder.appendLine("Here's something I can do:\n")

    val commandsByCategory = CommandManager.getCommands().values.groupBy { it.categoryId }

    for ((categoryId, commands) in commandsByCategory) {
        builder.appendLine(CommandManager.getCategory(categoryId)?.toString())

        for (command in commands)
            builder.appendLine(command.toString())

        builder.appendLine()
    }

    channel.sendMessage(builder.toString()).complete()
}
