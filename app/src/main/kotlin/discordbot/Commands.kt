package discordbot

import discordbot.commands.HelpCommand
import discordbot.commands.TestCommand
import discordbot.games.RockPaperScissors
import discordbot.games.registerGameCommand
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

typealias CommandFunction = (sender: User, channel: MessageChannel, args: String) -> Unit

class Command(val name: String, val description: String, val categoryId: String, val func: CommandFunction) {
    fun run(sender: User, channel: MessageChannel, args: String) {
        func(sender, channel, args)
    }

    override fun toString() = "- $botPrefix$name - $description"
}

typealias UserChannel = Pair<User, MessageChannel>
data class CommandCategory(val id: String, val name: String, val description: String) {
    override fun toString() = "**$name** - $description"
}

object CommandManager {
    private val commands: MutableMap<String, Command> = mutableMapOf()
    private val categories: MutableMap<String, CommandCategory> = mutableMapOf()

    private val nextMessageHandlers: MutableMap<UserChannel, CommandFunction> = mutableMapOf()

    fun getCommands(): Map<String, Command> = commands
    fun getCategory(id: String) = categories[id]

    private fun registerCategory(category: CommandCategory) {
        categories[category.id] = category
    }

    fun registerCommand(command: Command) {
        commands[command.name.lowercase()] = command
    }

    fun registerCommands() {
        registerCategory(CommandCategory("main", "Main commands", "Main commands"))
        registerCommand(HelpCommand)

        registerCategory(CommandCategory("fun", "Fun commands", "Commands for fun"))
        registerCommand(TestCommand)
        registerGameCommand("rps", "Rock-paper-scissors", 2)
        { _: User, _: MessageChannel, _: String ->
            RockPaperScissors()
        }
    }

    fun executeCommand(event: MessageReceivedEvent) {
        val message = event.message.contentRaw
        val split = message.substring(botPrefix.length).split(' ')
        val name = split[0].lowercase()
        val command = commands[name]
        if (command == null) {
            event.channel.sendMessage("Command \"$name\" does not exist")
            return
        }
        val args = split.drop(1)
        command.run(event.author, event.channel,
            if (args.isNotEmpty()) message.substring(botPrefix.length + name.length + 1) else "")
    }

    private fun resetNextMessageHandler(user: User, channel: MessageChannel) {
        nextMessageHandlers.remove(UserChannel(user, channel))
    }

    fun registerNextMessageHandler(user: User, channel: MessageChannel, handler: CommandFunction) {
        nextMessageHandlers[UserChannel(user, channel)] = handler
    }

    fun tryNextMessageHandler(event: MessageReceivedEvent): Boolean {
        val user = event.author
        val channel = event.channel
        val message = event.message.contentRaw
        val userChannel = UserChannel(user, channel)

        val handler = nextMessageHandlers[userChannel]
        if (handler != null) {
            resetNextMessageHandler(user, channel)
            handler(user, channel, message)
            return true
        }
        return false
    }
}