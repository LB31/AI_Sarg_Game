import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import lenz.htw.sarg.Move;
import lenz.htw.sarg.net.NetworkClient;

public class ClientSarg implements Runnable {
	private NetworkClient nc;

	private String playerName;
	private int playerNumber;

	private int[][] playfield = new int[9][9];
	private List<Token> tokenPositions = new ArrayList<Token>();
	private int[] scores = new int[3];

	private Vector2D[][] moveDirections = new Vector2D[][] { { new Vector2D(0, 1), new Vector2D(1, 1) },
			{ new Vector2D(1, 0), new Vector2D(0, -1) }, { new Vector2D(-1, -1), new Vector2D(-1, 0) } };

	// Alpha-Beta Pruning
	private int[][] playfieldAB = new int[9][9];
	private List<Token> tokenPositionsAB = new ArrayList<Token>();
	private int[] scoresAB = new int[3];

	public ClientSarg(String name) throws IOException {
		playerName = name;

	}

	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = rot, 1 = grün, 2 = blau

		for (int i = 0; i < 5; i++) {
			// red
			playfield[i][0] = 1;
			// green
			playfield[i][4 + i] = 2;
			// blue
			playfield[8][4 + i] = 3;

			Token posRed = new Token(i, 0, 0, playerNumber);
			Token posGreen = new Token(i, 4 + i, 1, playerNumber);
			Token posBlue = new Token(8, 4 + i, 2, playerNumber);

			tokenPositions.add(posRed);
			tokenPositions.add(posGreen);
			tokenPositions.add(posBlue);
		}

		// mark the dark side of the playfield
		for (int i = 1; i <= 4; i++) {
			for (int j = 0; j < i; j++) {
				playfield[j][5 + i - 1] = -1;
				playfield[8 - j][3 - i + 1] = -1;
			}

		}

//		if(playerNumber == 0) {
//			for (Token t : tokenPositions) {
//				System.out.println("X:" + t.x + " Y:" + t.y);
//			}
//			System.out.println(tokenPositions.size() + " Size");
//		}

	}

	@Override
	public void run() {

		try {
			nc = new NetworkClient("127.0.0.1", playerName, ImageIO.read(new File("./bilder/phoenix.jpg")));

			initialize();

			// Debug
//			System.out.println("Timelimit " + nc.getTimeLimitInSeconds());
//			System.out.println("getExpectedNetworkLatencyInMilliseconds " + nc.getExpectedNetworkLatencyInMilliseconds());
			if (playerNumber == 0)
				for (int[] row : playfield) {
					System.out.println(Arrays.toString(row));
				}
//			System.out.println(tokenPositions.get(4).x);

		} catch (IOException e) {
			e.printStackTrace();
		}

		update();
	}

	private void update() {
		while (true) {
			Move receivedMove = nc.receiveMove();
			if (receivedMove == null) {
				// ich bin dran
				Move move = calculateMove();
				nc.sendMove(move);
			} else {
				updatePlayfield(receivedMove);
			}
		}
	}

	private Move calculateMove() {
		Move move = null;

		// prepare temporary fields
		playfieldAB = playfield.clone();
		tokenPositionsAB = new ArrayList<Token>(tokenPositions);
		scoresAB = scores.clone();

//		int test = AlphaBeta(4, Integer.MIN_VALUE, Integer.MIN_VALUE, 1);

		Token[] myTokens = tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);
		int randomNum = ThreadLocalRandom.current().nextInt(0, myTokens.length);
		Token token = myTokens[randomNum];
		move = new Move((int) token.x, (int) token.y);

		return move;
	}

	public void updatePlayfield(Move move) {
		Token tokenToMove = tokenPositions.stream().filter(t -> t.x == move.x && t.y == move.y).findFirst().get();

		for (int i = 0; i < moveDirections[0].length; i++) {
			
			for (int j = 1; j <= playfield.length; j++) {
				// new Token
				int newX = (int) (tokenToMove.x + moveDirections[tokenToMove.owner][i].x * j);
				int newY = (int) (tokenToMove.y + moveDirections[tokenToMove.owner][i].y * j);
				try {
					int nextField = playfield[newX][newY];

					if (nextField == 0) { // The next field was empty
						playfield[newX][newY] = tokenToMove.owner + 1;
						tokenPositions.add(new Token(newX, newY, tokenToMove.owner, playerNumber));
						break;
					} else if (nextField == -1) { // still in array, but outside of the playing field
						scores[tokenToMove.owner]++;
						break;
					} else {
						// jumped over a token
						playfield[newX][newY] = 0;
						Token killedToken = tokenPositions.stream().filter(t -> t.x == newX && t.y == newY).findFirst()
								.get();
						tokenPositions.remove(killedToken);
					}
				} catch (IndexOutOfBoundsException e) { // point outside of the playing field array
					scores[tokenToMove.owner]++;
					break;
				}
			}
		}

		tokenPositions.remove(tokenToMove);
		playfield[move.x][move.y] = 0;

	}

	/**
	 * 
	 * @param player: 1 = max, 2 = min, 3 = min
	 */
	private int AlphaBeta(int depth, int alpha, int beta, int player) {
		// TODO Züge sortieren, z.B. danach, wv Felder bis zum Punkt noch gegangen
		// werden müssen
		if (depth == 0 /* TODO game over in current position */) {
			// TODO return evalation
			return calculateRating();
		}

		if (player == 1) {
			Token[] currentTokens = tokenPositionsAB.stream().filter(t -> t.mine).toArray(Token[]::new);
			for (Token token : currentTokens) {

			}
			int maxEval = Integer.MAX_VALUE;
		} else if (player == 2) {

		} else {

		}
		return 0;
	}

	private int calculateRating() {

		return 0;

	}

}
