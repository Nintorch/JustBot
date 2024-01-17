package discordbot.commands

import discordbot.Command
import discordbot.CommandManager
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

val TestCommand = Command("test", "Some description", "fun", ::start)

private data class NameAge(var name: String, var age: Int)
private val userMap: MutableMap<User, NameAge> = mutableMapOf()

private fun start(sender: User, channel: MessageChannel, args: String) {
    userMap[sender] = NameAge("", 0)
    channel.sendMessage("Hello, fellow user. What's your name?").complete()
    CommandManager.registerNextMessageHandler(sender, channel, ::getName)
}

private fun getName(sender: User, channel: MessageChannel, args: String) {
    userMap[sender]?.name = args
    channel.sendMessage("Hello, ${userMap[sender]?.name}! How old are you?").complete()
    CommandManager.registerNextMessageHandler(sender, channel, ::getAge)
}

private fun getAge(sender: User, channel: MessageChannel, args: String) {
    val nameAge: NameAge = userMap[sender] ?: throw Exception()
    nameAge.age = args.toInt()
    channel.sendMessage("You are ${nameAge.name} that is ${nameAge.age} years old!").complete()
}