package botsquared;


/**
 * Creates the bot, connects it to Twitch, and has it join a channel.
 * 
 */

public class BotsquaredMain {

	public static void main(String[] args) throws Exception {

		//Start bot
		Botsquared bot = new Botsquared();

		//Enable debugging output
		bot.setVerbose(true);

		//Connect to the IRC server
		bot.connect("oauth:x17ljvxnike9v1o7kwbhpklo8pk1u6");

		bot.joinChannel("#botsquared");

	}

}
