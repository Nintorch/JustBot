package discordbot.commands

import discordbot.Command
import discordbot.CommandManager
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

val HelpCommand = Command("help", "Show all commands", "main")
{ _: User, channel: MessageChannel, _: String ->
    val builder = StringBuilder()
    val commandsByCategory = CommandManager.getCommands().values.groupBy { it.categoryId }

    for ((categoryId, commands) in commandsByCategory) {
        builder.appendLine(CommandManager.getCategory(categoryId)?.toString())

        for (command in commands)
            builder.appendLine(command.toString())

        builder.appendLine()
    }

    channel.sendMessage(builder.toString()).complete()
}