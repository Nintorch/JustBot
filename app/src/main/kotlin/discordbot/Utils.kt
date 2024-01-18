package discordbot

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

val User.dms: MessageChannel get() = this.openPrivateChannel().complete()