# Command Documentation
To manage the permissions you need a role called "try-catch-perms", which gets created by the Bot if you use the permissions coommand.
## Block
Aliases: block, deny  
Usage: [Prefix][block|deny] @Member #Channel <reason>  
Description: Denies permission for a specific user in a specific channel.  
Permission level: 2  

## Clear
Aliases: clear, clean, delete  
Usage: [Prefix][clear|clean|delete] <amount> (amount must be a number between 1-9999)  
Description: Clears up to 9999 messages at a time. Though it is recommended to use it carefully. Deleting might take a while.  
Permission level: 4  

## GuildMute
Aliases: muteguild, guildmute, lockdown  
Usage: [Prefix][muteguild|guildmute|lockdown]  
Description: Mutes the whole guild so no one can write.  
Permission level: 3  

## Help
Aliases: help, helpme, command  
Usage: [Prefix][help|helpme|command]  
Description: Provides you with help.  
Permission level: 0  

## Invite
Aliases: invite  
Usage: [Prefix]invite  
Description: Opens the invite dialogue where you can choose between an invite for the bot or this guild.  
Permission level: 1  

## Link
Aliases: link  
Usage: [Prefix]link [request|accept|disconnect|invite]  
Note that you may only use invite and disconnect if you're currently connected.  

A guild may only have one link or one request at a time. As soon as you request a link, your guild is linked.
In order to send a new request, you need to disconnect first.  

Description: Links your guild with up to 9 other guilds with a channel.  
Permission level: 4  

## Mail
Aliases: mail, contact  
Usage: [Prefix][mail|contact] <Guild-ID> <Message>  

If you don't have an id, replace it with "NOID". You may then search a guild by name.
To add a topic to your mail, put ##your-topic## somewhere (replace "your-topic" with the topic you want).
This only works if the other guild has set a mail channel.  

Description: Send a "mail" to a guild the bot is also on!  
Permission level: 4  

## Mute
Aliases: mute, silence  
Usage: [Prefix][mute|silence] @Member <reason>  
Description: Mutes a member so that he can't write in the whole guild.  
Permission level: 3  

## Permission
Aliases: permission, perm, perms  
Usage: [Prefix][permission|perms|perm] [<@Member>|<@Role>] <level> (level may be 0-5)  
Description: Manage Dev-Bot-permissions and configure the different permission levels.  
Level 0: -help and -ttt  
Level 1: -profile, -search, -invite, -xp, -scoreboard  
Level 2: -block  
Level 3: -mute, -muteguild and -report  
Level 4: -vote, -mail, -link, -clear  
Level 5: -settings and -role  
Permission level: To execute this command, the member needs to have a role named "try-catch-perms".  

## Profile
Aliases: profile, userinfo  
Usage: [Prefix][profile|userinfo] to view your profile [Prefix][profile|userinfo] @Member to view @Member's profile  
Description: Provides you with Information about yourself or another member.  
Permission level: 1  

## Report
Aliases: report, rep, reports  
Usage:  [Prefix][report|rep|reports] @Member <reason> to report [Prefix][rep|report] [get|remove] @Member <index> to manage  
Description: Reports a given member. After 3 (default is 3) reports, a member will be banned. To change this, make use of the settings command.  
Permission level: 3  

## Role
Aliases: role  
Usage: [Prefix]role [add|remove] @Member <role>  
Description: Grants or removes a specific role from a member.  
Permission level: 5  

## ScoreBoard
Aliases: scoreboard, sb  
Usage: [Prefix][scoreboard|sb]  
Description: Gives you information about your score and the best scores.  
Permission level: 1  

## Search
Aliases: search, looksfor, browse  
Usage:  [Prefix][search|looksfor|browse] [user|guild] <name> | [Prefix]search display [users|guilds]  
Description: Let's you search for a specific user/guild. Or let's you display all users/guilds.  
Permission level: 2  

## Settings
Aliases: settings, control  
Usage: [Prefix][settings|control]  
Description: Opens the settings dialogue for this bot.  
Permission level: 5  

## TicTacToe
Aliases: tictactoe, ttt  
Usage: [Prefix][ttt|tictactoe]  
Description: Starts a game of Tic-Tac-Toe.  
Permission level: 0  

## Vote
Aliases: vote, poll  
Usage: [Prefix][vote|poll] | [Prefix][vote|poll] close  
Description: Creates a vote and evaluates it into a percentage value and a piechart. If you desire you can close the vote early. 
Permission level: 4  

## XP
Aliases: xp, lvl, level  
Usage: [Prefix][xp|lvl|level]  
Description: Gives you information about your level.  
Permission level: 1  