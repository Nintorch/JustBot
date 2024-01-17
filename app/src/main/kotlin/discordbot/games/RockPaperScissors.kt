package discordbot.games

import discordbot.CommandManager
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

private val User.dms: MessageChannel get() = this.openPrivateChannel().complete()
private val itemNames = listOf("rock", "paper", "scissors")

private enum class Result {
    WIN,
    LOSS,
    DRAW,
}

class RockPaperScissors : TextGame() {
    override val name get() = "Rock-Paper-Scissors"
    override val description get() = "Play rock-paper-scissors in Discord with your friend!"

    var currentUserID = 0
    val userChoices = arrayOf(0, 0)
    var users: List<User> = listOf()

    override fun start(users: List<User>) {
        this.users = users
        prepareProcessUser(users[0])
        users[1].dms.sendMessage("Your opponent is making their choice").complete()
    }

    private fun prepareProcessUser(user: User) {
        user.dms.sendMessage("Choose: rock, paper or scissors").complete()
        CommandManager.registerNextMessageHandler(user, user.dms, this::processUserMessage)
    }

    private fun processUserMessage(sender: User, channel: MessageChannel, args: String) {
        val text = args.lowercase()
        val choice = itemNames.indexOf(text)

        if (choice < 0) {
            channel.sendMessage("Invalid input, try again").complete()
            prepareProcessUser(sender)
            return
        }

        userChoices[currentUserID] = choice
        currentUserID++
        if (currentUserID == 2) {
            gameFinish()
        }
        else {
            users[0].dms.sendMessage("Alright, asking the second user now").complete()
            prepareProcessUser(users[1])
        }
    }

    private fun gameFinish() {
        sendResults(0, 1)
        sendResults(1, 0)
    }

    private fun sendResults(currentUser: Int, opponent: Int) {
        val user = users[currentUser]
        val builder = StringBuilder()
        val currentChoice = userChoices[currentUser]
        val opponentChoice = userChoices[opponent]
        builder.appendLine("You chose ${itemNames[currentChoice]}, " +
                "your opponent chose ${itemNames[opponentChoice]}\n")
        val message = when (checkWin(currentChoice, opponentChoice)) {
            Result.WIN -> "You won!"
            Result.LOSS -> "You lost."
            Result.DRAW -> "Draw"
        }
        builder.append(message)
        user.dms.sendMessage(builder.toString()).complete()
    }

    private fun checkWin(currentChoice: Int, opponentChoice: Int): Result {
        if (currentChoice == opponentChoice)
            return Result.DRAW

        if ((currentChoice == 0 && opponentChoice == 2) ||
            (currentChoice == 1 && opponentChoice == 0) ||
            (currentChoice == 2 && opponentChoice == 1))
            return Result.WIN

        return Result.LOSS
    }
}