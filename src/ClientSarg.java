import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

	private Vector2D[][] moveDirections = new Vector2D[][] { 
			{ new Vector2D(0, 1), new Vector2D(1, 1) },
			{ new Vector2D(1, 0), new Vector2D(0, -1) }, 
			{ new Vector2D(-1, -1), new Vector2D(-1, 0) } 
			};

	private int[] scores = new int[3];

	public ClientSarg(String name) throws IOException {
		playerName = name;

	}

	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = rot, 1 = grün, 2 = blau
		System.out.println(playerNumber + " number");

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
				playfield[j][5+i-1] = -1;
				playfield[8-j][3-i+1] = -1;
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
			if(playerNumber == 0)
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
		Token[] myTokens = tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);

		int randomNum = ThreadLocalRandom.current().nextInt(0, myTokens.length);
		Token token = myTokens[randomNum];

//		System.out.println("Anzahl meiner Token: " + myTokens.length);
//		System.out.println("Gewählter Token: " + randomNum);

		move = new Move((int) token.x, (int) token.y);
//		System.out.println("Kommender move: " + move.x + " X " + move.y + " Y");
		return move;
	}

	// TODO doch nichts returnen, mergen mit update field
	public void getResultingPosition(Token tokenToMove, int leftRight) {
		for (int i = 1; i <= playfield.length; i++) {
			// new left Token
			int newX = (int) (tokenToMove.x + moveDirections[tokenToMove.owner][leftRight].x * i);
			int newY = (int) (tokenToMove.y + moveDirections[tokenToMove.owner][leftRight].y * i);
			try {
				int nextField = playfield[newX][newY];
				
				if(nextField == 0) { // The next field was empty
					playfield[newX][newY] = tokenToMove.owner + 1;
					tokenPositions.add(new Token(newX, newY, tokenToMove.owner, playerNumber));
					break;
				}
				else if(nextField == -1) { // Außerhalb des Spielfels, jedoch noch im Array
					scores[tokenToMove.owner]++;
					System.out.println(Arrays.toString(scores));
					break;
				}
				else {
					// TODO übersprungenen Stein handlen
					playfield[newX][newY] = 0;
					Token killedToken = tokenPositions.stream().filter(t -> t.x == newX && t.y == newY).findFirst().get();
					tokenPositions.remove(killedToken);
				}
			} catch (IndexOutOfBoundsException e) { // 
				scores[tokenToMove.owner]++;
				System.out.println("Error Punkt!");
				System.out.println(Arrays.toString(scores));
				break;
			}
		}

    }

	private void updatePlayfield(Move move) {
		Token movedToken = tokenPositions.stream().filter(t -> t.x == move.x && t.y == move.y).findFirst().get();

		getResultingPosition(movedToken, 0);
		getResultingPosition(movedToken, 1);
		// Get rid of old token
		tokenPositions.remove(movedToken);
		playfield[move.x][move.y] = 0;

//		int ownerNumber = playfield[move.x][move.y] - 1;
//
//		playfield[(int) (move.x + moveDirections[ownerNumber][0].x)][(int) (move.y
//				+ moveDirections[ownerNumber][0].y)] = ownerNumber + 1;
//		playfield[(int) (move.x + moveDirections[ownerNumber][1].x)][(int) (move.y
//				+ moveDirections[ownerNumber][1].y)] = ownerNumber + 1;
//
//		if (ownerNumber == 0) { // red
//			playfield[move.x][move.y + 1] = 1;
//			playfield[move.x + 1][move.y + 1] = 1;
//			tokenPositions.add(new Token(move.x, move.y + 1, ownerNumber, playerNumber));
//			tokenPositions.add(new Token(move.x + 1, move.y + 1, ownerNumber, playerNumber));
//		} else if (ownerNumber == 1) { // green
//			playfield[move.x + 1][move.y] = 2;
//			playfield[move.x][move.y - 1] = 2;
//			tokenPositions.add(new Token(move.x + 1, move.y, ownerNumber, playerNumber));
//			tokenPositions.add(new Token(move.x, move.y - 1, ownerNumber, playerNumber));
//		} else { // blue
//			playfield[move.x - 1][move.y - 1] = 3;
//			playfield[move.x - 1][move.y] = 3;
//			tokenPositions.add(new Token(move.x - 1, move.y - 1, ownerNumber, playerNumber));
//			tokenPositions.add(new Token(move.x - 1, move.y, ownerNumber, playerNumber));
//		}

	}

	/**
	 * 
	 * @param maximazingPlayer 1 = max, 2 = min, 3 = min
	 */
	private void /* TODO return something */ AlphaBeta(Token position, int depth, int alpha, int beta,
			int maximazingPlayer) {
		// TODO Züge sortieren, z.B. danach, wv Felder bis zum Punkt noch gegangen
		// werden müssen
		if (depth == 0 /* TODO game over in current position */) {
			// TODO return something, probably the best Move
		}

		if (maximazingPlayer == 1) {

		} else if (maximazingPlayer == 2) {

		} else {

		}
	}

}
