import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class MLPlayerAlphaRandom
{
	// An internal board representation.
	private int internal_board[][] = null;

	// The player.
	Player thePlayer;

	// The player id of this player.
	int player_id = 0;

	// Constructor.
	public MLPlayerAlphaRandom(Player p)
	{
		thePlayer=p;
	}

	// The play function, which connects to the socket and plays.
	public void play(int my_id) throws IOException,ClassNotFoundException
	{
		// Open socket and in/out streams.
		Socket sock = new Socket("localhost", Player.getSocketNumber(thePlayer));
		ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
		BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

		// Set the id.
		player_id = my_id;

		// Get the game rules.
		Rules gameRules = (Rules)in.readObject();
		System.out.printf("Num Rows: %d, Num Cols: %d, Num Connect: %d\n", gameRules.numRows, gameRules.numCols, gameRules.numConnect);

		// Create the internal board.
		internal_board = new int[gameRules.numRows][gameRules.numCols];
		for (int r = 0; r < gameRules.numRows; r++)
			for (int c = 0; c < gameRules.numCols; c++)
				internal_board[r][c] = 0;

		// Start playing the game, first by waiting fo the initial message.
		System.out.println("Waiting...");
		GameMessage mess = (GameMessage)in.readObject();

		// The main game loop.
		int move = 0;
		while(mess.win == Player.EMPTY)
		{
			// If the first message is not the begin message (-1), then record what the other player did.
			if(mess.move != -1)
			{
				int r = 0;
				for (r = 0; r < gameRules.numRows; r++)
				{
					if (internal_board[r][mess.move] != 0)
					{
						internal_board[r - 1][mess.move] = player_id % 2 + 1;
						break;
					}
				}
				if (r == gameRules.numRows) internal_board[r - 1][mess.move] = player_id % 2 + 1;
			}

/*
			// Create features based on the current board layout.
			FeatureFinder feature_finder = new FeatureFinder(internal_board, gameRules.numRows, gameRules.numCols);
			ArrayList<Feature> features = feature_finder.FindFeatures();

System.out.println("Features Found:");
for (int i = 0; i < features.size(); i++) System.out.println("(" + features.get(i).start.r + ", " + features.get(i).start.c + ")\t" + features.get(i).theta + "\t" + features.get(i).length + "\t" + features.get(i).type);

*/

			// Decide on which column to place the token.
			int selected_column = 0;
			do
			{
				selected_column = (int)((float)gameRules.numCols * Math.random());
			}
			while (internal_board[0][selected_column] != 0);

			// Update the internal representation for where this player put his token.
			int r = 0;
			for (r = 0; r < gameRules.numRows; r++)
			{
				if (internal_board[r][selected_column] != 0)
				{
					internal_board[r - 1][selected_column] = player_id;
					break;
				}
			}
			if (r == gameRules.numRows) internal_board[r - 1][selected_column] = player_id;

			// Write the game message and read the next one.
			mess.move = selected_column;
			out.writeObject(mess);
			mess = (GameMessage)in.readObject();
		}

		// Close the socket.
		sock.close();
	}

	// The main function.
	public static void main(String[] args)
	{
		// If no argument is specified, throw an error.
		if(args.length != 1)
		{
	    		System.out.println("Usage:\n java MPLayerAlpha [1|2]");
	    		System.exit(-1);
		}

		// Get the player.
		int my_id = Integer.parseInt(args[0]);

		// Set the player object.
		Player p = null;
		if (my_id == 1) p = Player.ONE;
		else if (my_id == 2) p = Player.TWO;
		else
		{
			System.out.println("Usage:\n java MPLayerAlpha [1|2]");
			System.exit(-1);
		}

		// Create the MLPlayer object, and begin play.
		MLPlayerAlphaRandom me = new MLPlayerAlphaRandom(p);
		try
		{
			me.play(my_id);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
	}
}

