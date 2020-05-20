import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import lenz.htw.sarg.Move;
import lenz.htw.sarg.net.NetworkClient;

public class ClientSarg implements Runnable {
	private NetworkClient nc;

	private String playerName;
	private int playerNumber;

	private EvaluationFunction evaFunc;

	private PlayingField mainField;
	// For Alpha-Beta Pruning
//	private List<PlayingField> alphaBetaFields = new ArrayList<PlayingField>();
	private Stack<PlayingField> alphaBetaFields = new Stack<>();

	private Vector2D[][] moveDirections = new Vector2D[][] { { new Vector2D(0, 1), new Vector2D(1, 1) },
			{ new Vector2D(1, 0), new Vector2D(0, -1) }, { new Vector2D(-1, -1), new Vector2D(-1, 0) } };

	private List<Integer> otherPlayers = new ArrayList<>() {{
			add(0);
			add(1);
			add(2);
		}};

	private int searchDepth = 4;

	private Token bestTokenToMove;

	public ClientSarg(String name, EvaluationFunction eva) throws IOException {
		playerName = name;
		evaFunc = eva;
	}

	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = red, 1 = green, 2 = blue
		otherPlayers.remove(playerNumber);

		mainField = new PlayingField();

		for (int i = 0; i < 5; i++) {
			// red
			mainField.playfield[i][0] = 1;
			// green
			mainField.playfield[i][4 + i] = 2;
			// blue
			mainField.playfield[8][4 + i] = 3;

			Token posRed = new Token(i, 0, 0, playerNumber);
			Token posGreen = new Token(i, 4 + i, 1, playerNumber);
			Token posBlue = new Token(8, 4 + i, 2, playerNumber);

			mainField.tokenPositions.add(posRed);
			mainField.tokenPositions.add(posGreen);
			mainField.tokenPositions.add(posBlue);
		}

		// mark the dark side of the playfield
		for (int i = 1; i <= 4; i++) {
			for (int j = 0; j < i; j++) {
				mainField.playfield[j][5 + i - 1] = -1;
				mainField.playfield[8 - j][3 - i + 1] = -1;
			}

		}

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
				for (int[] row : mainField.playfield) {
					System.out.println(Arrays.toString(row));
				}
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
				updatePlayfield(receivedMove, false);
			}
		}
	}

	private PlayingField newPlayingFieldCopy(PlayingField pf) {
		return new PlayingField(pf.playfield.clone(), new ArrayList<>(pf.tokenPositions), pf.scores.clone());
	}

	private Move calculateMove() {
		Move move = null;

		alphaBetaFields.push(newPlayingFieldCopy(mainField));
		AlphaBeta(searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		alphaBetaFields.empty();

//		Token[] myTokens = mainField.tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);
//		int randomNum = ThreadLocalRandom.current().nextInt(0, myTokens.length);
//		Token token = myTokens[randomNum];
		Token token = bestTokenToMove;
		move = new Move(token.x, token.y);

		return move;
	}

	public void updatePlayfield(Move move, boolean alphaBeta) {
		PlayingField playingField;

		if (alphaBeta) {
			// create a new playing field with the stats from the last move
//			playingField = newPlayingFieldCopy(alphaBetaFields.get(alphaBetaFields.size() - 1));
			playingField = newPlayingFieldCopy(alphaBetaFields.peek());
		} else { // for the actual updating of the original field
			playingField = mainField;
		}

//		Token tokenToMove = playingField.tokenPositions.stream().filter(t -> t.x == move.x && t.y == move.y).findAny()
//				.get();
		Token tokenToMove = null;
		for (Token token : playingField.tokenPositions) {
			if(token.x == move.x && token.y == move.y) tokenToMove = token;
		}
		
		for (int i = 0; i < moveDirections[0].length; i++) {

			for (int j = 1; j <= playingField.playfield.length; j++) {
				// new Token
				int newX = (int) (tokenToMove.x + moveDirections[tokenToMove.owner][i].x * j);
				int newY = (int) (tokenToMove.y + moveDirections[tokenToMove.owner][i].y * j);
				try {
					int nextField = playingField.playfield[newX][newY];

					if (nextField == 0) { // The next field was empty
						playingField.playfield[newX][newY] = tokenToMove.owner + 1;
						playingField.tokenPositions.add(new Token(newX, newY, tokenToMove.owner, playerNumber));
						break;
					} else if (nextField == -1) { // still in array, but outside of the playing field
						playingField.scores[tokenToMove.owner]++;
						break;
					} else {
						// jumped over a token
						playingField.playfield[newX][newY] = 0;
//						boolean error = playingFieldDebugger(playingField);
//
//						Optional<Token> killedToken = playingField.tokenPositions.stream().filter(t -> t.x == newX && t.y == newY)
//								.findFirst();
//						boolean empty = true;
//						if(killedToken.isEmpty()) empty = true;
						Token finalToken = null;
						for (Token token : playingField.tokenPositions) {
							if(token.x == newX && token.y == newY) finalToken = token;
						}
						playingField.tokenPositions.remove(finalToken);
					}
				} catch (IndexOutOfBoundsException e) { // point outside of the playing field array
					playingField.scores[tokenToMove.owner]++;
					break;
				}
			}
		}

		playingField.tokenPositions.remove(tokenToMove);
		playingField.playfield[move.x][move.y] = 0;

		if (alphaBeta) {
//			alphaBetaFields.add(playingField);		
			alphaBetaFields.push(playingField);
		}

	}

	/**
	 * 
	 * @param player: 1 = max, 2 = min, 3 = min
	 */
	private int AlphaBeta(int depth, int alpha, int beta, int player) {
//		System.out.println(depth + " current depth");
//		System.out.println(alphaBetaFields.size() + " size list");
		// TODO Züge sortieren, z.B. danach, wv Felder bis zum Punkt noch gegangen
		// werden müssen
		if (depth == 0 /* TODO game over in current position */) {
//			return calculateRating(alphaBetaFields.get(alphaBetaFields.size() - 1));
			return calculateRating(alphaBetaFields.peek());
		}

		// TODO Spielfeld 2D auf Referenzen umstellen
		if (player == 1) {
			int maxEval = alpha;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream().filter(t -> t.mine)
					.toArray(Token[]::new);
//					alphaBetaFields.get(searchDepth - depth).tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				int eval = AlphaBeta(depth - 1, maxEval, beta, 2);
//				alphaBetaFields.remove(searchDepth - depth); // undo move
				alphaBetaFields.pop();
//				maxEval = Math.max(maxEval, eval);
				if (eval > maxEval) {
					maxEval = eval;
					if (depth == searchDepth)
						bestTokenToMove = token;
					if (maxEval > beta)
						break; // pruning
				}

			}
			return maxEval;

		} else if (player == 2) {
			int minEval = beta;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream()
					.filter(t -> t.owner == otherPlayers.get(0)).toArray(Token[]::new); // get is hard coded here
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				int eval = AlphaBeta(depth - 1, alpha, minEval, 3);
//				alphaBetaFields.remove(searchDepth - depth); // undo move
				alphaBetaFields.pop();
				if (eval < minEval) {
					minEval = eval;
					if (minEval <= alpha)
						break; // pruning
				}
			}
			return minEval;
		} else {
			int minEval = beta;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream()
					.filter(t -> t.owner == otherPlayers.get(1)).toArray(Token[]::new); // get is hard coded here
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				int eval = AlphaBeta(depth - 1, alpha, minEval, 1);
//				alphaBetaFields.remove(searchDepth - depth); // undo move
				alphaBetaFields.pop();
				if (eval < minEval) {
					minEval = eval;
					if (minEval <= alpha)
						break; // pruning
				}
			}
			return minEval;
		}

	}

	private int calculateRating(PlayingField currentField) {
		int rating = 0;
		rating += evaFunc.a * currentField.tokenPositions.stream().filter(t -> t.mine).count();
		rating -= evaFunc.b * currentField.tokenPositions.stream().filter(t -> t.owner == otherPlayers.get(0)).count();
		rating -= evaFunc.c * currentField.tokenPositions.stream().filter(t -> t.owner == otherPlayers.get(1)).count();

		// TODO

		return rating;

	}

	private boolean playingFieldDebugger(PlayingField currentField) {
		boolean error = true;
		for (int row = 0; row < currentField.playfield.length; row++) {
            for (int col = 0; col < currentField.playfield[row].length; col++) {
            	
            	int curVal = currentField.playfield[row][col];
            	int r = row; int c = col;
            	if(curVal != 0 && curVal != -1) {
            		for (Token token : currentField.tokenPositions) {
						if(token.x == r && token.y == col) error = false;
					}
            	}
            	
            }
        }
		return error;
	}
	
}
