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
		bot.connect("");

		bot.joinChannel("#botsquared");

	}

}
