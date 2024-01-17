package discordbot.commands

import discordbot.Command
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

val GreetCommand = Command("greet", "Greet the user", "main")
{ sender: User, channel: MessageChannel, args: String ->
    channel.sendMessage("Hello, ${sender.effectiveName}, my name is JustBot " +
            "and I was written in Kotlin using JDA").complete()
}