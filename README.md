Botsquared
==========

Botsquared is an IRC chat bot designed for Twitch.TV streams. It is built using Java, and uses JSON to keep track of custom commands and other customizable values.

# Commands
Commands allow users to communicate with the bot. Commands are made of six fields: name, level, access, global, delay, and output. For example:

-Name: !bot
-Level: NATIVE
-Access: PUBLIC
-Global: true
-Delay: 30
-Output: "Hello, I am Botsquared, a bot created by Bearsquared."

The Name must be **at least 3 letters or numbers and preceded by a '!'**. For example, '!cow' is a valid command name, 'cow' is not a valid command name, and '!ox' is not a valid command name.

The Level field determines who can edit or remove the command. The Level field can have the value **MOD, OWNER, NATIVE, or COMPLEX**. Custom commands may only have a Level of MOD, or OWNER which means that the command may only be edited or removed by a moderator, or channel owner respectively. The values NATIVE, COMPLEX are only used for commands that built into the bot and may not be edited, or removed by anyone.

The Access field determines who may call the command. The Access field can have the value **PUBLIC, SUB, MOD, or OWNER**. Commands with the Access PUBLIC may be called by anyone. Commands with the Access SUB may be called by subscribers, moderators, or the channel owner.

By default, the bot will only respond to a command every 5 seconds. There is a global delay of 5 seconds between each command. The Global field can be either **true or false**. If the Global field is true, the bot will respect the global delay for the command. If the Global field is false, the bot will ignore the global delay for the command. 

All commands have an Local Delay that is 30 seconds long by default. The Delay field can be edited to make the delay **0 seconds to 3600 seconds** (1 hour) long.

The Output is what the bot will say when called. The Output can be anything as long as it is **at least one character, and does not start with `<` and end with `>`**. Users call a command by typing it's name in chat and the bot will respond with the output. For example:

>**User:** !bot <br />**Botsquared:** Hello, I am Botsquared, a bot created by Bearsquared.

## Native Commands

**!bot:** This command will output the bot's name and creator.

**!command:** This command is used to add, edit, and remove custom commands. Type "!command add|edit|remove" to respectively add, edit, or remove commands.

-*add:* To add a command, type "!command add" followed by the name of the command, and the output. For example, `!command add !color1 Red` would add a new command named "!color1" with the output "Red". Keep in mind that the name of commands must be unique. If there was already a command named "!color1" then the bot would not let you add another. Optionally, you may also add a command using custom field values. For example, `!command add !color2 <level:OWNER> <access:MOD> <delay:60> Blue` would add a new command named "!color2", the output "Blue", that may only be edited or removed by the channel owener, and called by a moderator or channel owner every 60 seconds.

-*edit:* To edit a command, type "!command edit" followed by the name of the command, and any new field value. For example, `!command edit !color1 Green` would edit a command named "!color1" and change its output to "Green". You can only edit an existing command. If there is not a command named "!color1" then the bot would not be able to edit anything. You may also edit any of a command's field values. For example, `!command edit !color1 <level:OWNER> <access:MOD> <delay:60> Green` would change a command named "!color1" so that it may only be edited or removed by the channel owener, only be called by a moderator or channel owner every 60 seconds, and with the output "Green". As long as one of the values you give the bot is new, it will able to edit your command.

-*remove:* To remove a command, type "!command edit" followed by the name of the command. For example, `!command remove !color1` would remove a command named "!color1". You can only remove a existing command. If there is not a command named "!color1" then the bot would not be able to remove anything. If there is anything in the message following the command name, the bot will not remove anything.

**!list:** This command will output the channel's custom commands.

**!moderate:** This command is used to configure if and how the bot moderates the channel. The bot must be a moderator in the channel for it to moderate. Type `!moderate links` to have the bot automically delete links. Moderators may use "!permit" followed the name of a user to allow them to post exactly one link in chat.

**!poll:** "This command allows you to create a poll for your viewers in the chat. Type "!poll open|close|reset|results" to respectively open, close, reset, or the display the results of a poll.

-*open:* Opening a poll allows users to vote in chat. To open a poll, type "!poll open option1 | option2 | ...".  For example, `!poll open red | blue | green` would open a poll with the options red, blue, and green. The options are assigned a number when the poll is opened and will be posted in the chat. Red, blue, and green would be options 1, 2, and 3 respectively. There may only be one poll open at a time.

-*close:* Closing a poll prevents further voting and displays the results. To close a poll, type `!poll close`. A poll must be manually closed.

-*reset:* Reseting a poll will set the number of votes for each option to 0. To reset a poll, type `!poll reset`. Any user that had previously cast their vote may not vote again.

-*results:* This will display the current number of votes for each options. To show the results of a poll, type `!poll results`. This will only display the result. It will not close the poll.

-*!vote:* This allows a user to vote in a poll. To vote, type "!vote" followed by the number of the option you would like to vote for. In the example poll above, `!vote 1` would cast one vote for "red" where "!vote 2" would cast one vote for "blue". Users may only vote once per poll. If a poll is reset, they may vote again.

**!subnotify:** This command allows you to set what the bot posts when you have a subscriber. To set the subscriber notification, type "!subnotify" followed by your message. Every instance of `<user>` in the message will be replaced by the name of the subscriber.

**!uptime:** This command will display the amount of time the stream has been live.

# Credits
[PircBot Java API](http://www.jibble.org/pircbot.php) is what the Botsquared framework is based on.
[GSON](https://code.google.com/p/google-gson/) for parsing JSON.
