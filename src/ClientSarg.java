import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

	private List<Integer> otherPlayers = new ArrayList<>() {
		{
			add(0);
			add(1);
			add(2);
		}
	};

	private int searchDepth = 6;

	private Token bestTokenToMove;

	private int pruningCounter;

	public ClientSarg(String name, EvaluationFunction eva) throws IOException {
		playerName = name;
		evaFunc = eva;
	}

	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = red, 1 = green, 2 = blue
		otherPlayers.remove(playerNumber);

		mainField = new PlayingField();

		for (int i = 0; i < 5; i++) {
			Token posRed = new Token(i, 0, 0, playerNumber);
			Token posGreen = new Token(i, 4 + i, 1, playerNumber);
			Token posBlue = new Token(8, 4 + i, 2, playerNumber);

			mainField.tokenPositions.add(posRed);
			mainField.tokenPositions.add(posGreen);
			mainField.tokenPositions.add(posBlue);

			// red
			mainField.playfield[i][0] = posRed;
			// green
			mainField.playfield[i][4 + i] = posGreen;
			// blue
			mainField.playfield[8][4 + i] = posBlue;

		}

		// mark the dark side of the playfield
		for (int i = 1; i <= 4; i++) {
			for (int j = 0; j < i; j++) {
				int posX = j;
				int posY = 5 + i - 1;
				Token dummy1 = new Token(posX, posY, -1, -2);
				mainField.playfield[posX][posY] = dummy1;
				posX = 8 - j;
				posY = 3 - i + 1;
				Token dummy2 = new Token(posX, posY, -1, -2);
				mainField.playfield[posX][posY] = dummy2;
			}

		}

		// store the remaining fields to go for all Tokens
		for (Token token : mainField.tokenPositions) {
			StoreRemainingFieldAmount(token, mainField.playfield);
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
				for (Token[] row : mainField.playfield) {
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
		/*
		 * Not cloning the actual Token objects is ok, because even setting them to null
		 * don't destroy them when they are still referenced somewhere else in the stack
		 */
		Token[][] playfieldCopy = Arrays.stream(pf.playfield).map(Token[]::clone).toArray(Token[][]::new);
		int[] scoresCopy = Arrays.copyOf(pf.scores, pf.scores.length);
		return new PlayingField(playfieldCopy, new ArrayList<>(pf.tokenPositions), scoresCopy);
	}

	private Move calculateMove() {
		Move move = null;

		alphaBetaFields.push(newPlayingFieldCopy(mainField));
		AlphaBeta(searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		alphaBetaFields.empty();
		System.out.println(pruningCounter + " times geprunt von Spieler " + playerNumber);

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
			playingField = newPlayingFieldCopy(alphaBetaFields.peek());
		} else { // for the actual updating of the original field
			playingField = mainField;
		}

//		Token tokenToMove = playingField.tokenPositions.stream().filter(t -> t.x == move.x && t.y == move.y).findAny()
//				.get();
		Token tokenToMove = playingField.playfield[move.x][move.y];

		for (int i = 0; i < moveDirections[0].length; i++) { // for new Tokens on the left and right side

			for (int j = 1; j <= playingField.playfield.length; j++) {
				// new Token
				int nextX = (int) (tokenToMove.x + moveDirections[tokenToMove.owner][i].x * j);
				int nextY = (int) (tokenToMove.y + moveDirections[tokenToMove.owner][i].y * j);
				try {
					Token nextFieldToken = playingField.playfield[nextX][nextY];

					if (nextFieldToken == null) { // The next field was empty
						Token newToken = new Token(nextX, nextY, tokenToMove.owner, playerNumber);
						StoreRemainingFieldAmount(newToken, playingField.playfield);
						playingField.playfield[nextX][nextY] = newToken;
						playingField.tokenPositions.add(newToken);
						break;
					} else if (nextFieldToken.owner == -1) { // still in array, but outside of the playing field
						playingField.scores[tokenToMove.owner]++;
						break;
					} else {
						// jumped over a token
						Token killedToken = playingField.playfield[nextX][nextY];
						playingField.playfield[nextX][nextY] = null;
						playingField.tokenPositions.remove(killedToken);
					}
				} catch (IndexOutOfBoundsException e) { // point outside of the playing field array
					playingField.scores[tokenToMove.owner]++;
					break;
				}
			}
		}

		// Get rid of moved Token
		playingField.tokenPositions.remove(tokenToMove);
		playingField.playfield[move.x][move.y] = null;

		if (alphaBeta) {
			alphaBetaFields.push(playingField);
		}

	}

	private void StoreRemainingFieldAmount(Token token, Token[][] curPlayfield) {
		int[] remainingFields = new int[2];
		for (int i = 0; i < 2; i++) { // heading left and right
			for (int j = 0; j < curPlayfield.length; j++) {
				int nextX = (int) (token.x + moveDirections[token.owner][i].x * j);
				int nextY = (int) (token.y + moveDirections[token.owner][i].y * j);
				try {
					// still on playing field
					if (curPlayfield[nextX][nextY] == null || curPlayfield[nextX][nextY].owner != -1) {
						remainingFields[i]++;
					} else { // left field but still in array
						break;
					}
				} catch (IndexOutOfBoundsException e) { // left field
					break;
				}

			}
		}

		token.fieldsToGo = Math.min(remainingFields[0], remainingFields[1]);
	}

	/**
	 * 
	 * @param player: 1 = max, 2 = min, 3 = min
	 */
	private int AlphaBeta(int depth, int alpha, int beta, int player) {
// TODO Züge sortieren, z.B. danach, wv Felder bis zum Punkt noch gegangen werden müssen

		if (depth == 0 /* TODO game over in current position */) {
			return calculateRating(alphaBetaFields.peek());
		}

		if (player == 1) {
			int maxEval = alpha;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream().filter(t -> t.mine)
					.toArray(Token[]::new);
			// start with Tokens with the shortest way to go, to find the best move faster
			Arrays.sort(currentTokens, Comparator.comparing(Token::getFieldsToGo));
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				int eval = AlphaBeta(depth - 1, maxEval, beta, 2);
				alphaBetaFields.pop(); // undo move
//				maxEval = Math.max(maxEval, eval);
				if (eval > maxEval) {
					maxEval = eval;
					if (depth == searchDepth)
						bestTokenToMove = token;
					if (maxEval >= beta) {
						pruningCounter++;
						break; // pruning
					}

				}

			}
			return maxEval;

		} else if (player == 2) {
			int minEval = beta;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream()
					.filter(t -> t.owner == otherPlayers.get(0)).toArray(Token[]::new); // get is hard coded here
			// start with Tokens with the shortest way to go, to find the best move faster
			Arrays.sort(currentTokens, Comparator.comparing(Token::getFieldsToGo));
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				int eval = AlphaBeta(depth - 1, alpha, minEval / 2, 3);
				alphaBetaFields.pop(); // undo move
				if (eval < minEval) {
					minEval = eval;
					if (minEval <= alpha) {
						pruningCounter++;
						break; // pruning
					}

				}
			}
			return minEval;
		} else {
			int minEval = beta;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream()
					.filter(t -> t.owner == otherPlayers.get(1)).toArray(Token[]::new); // get is hard coded here
			// start with Tokens with the shortest way to go, to find the best move faster
			Arrays.sort(currentTokens, Comparator.comparing(Token::getFieldsToGo));
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				int eval = AlphaBeta(depth - 1, alpha, minEval / 2, 1);
				alphaBetaFields.pop(); // undo move
				if (eval < minEval) {
					minEval = eval;
					if (minEval <= alpha) {
						pruningCounter++;
						break; // pruning
					}
				}
			}
			return minEval;
		}

	}

	private int calculateRating(PlayingField currentField) {
		int rating = 0;
		// Token amounts
		rating += evaFunc.a * currentField.tokenPositions.stream().filter(t -> t.mine).count();
		rating -= evaFunc.b * currentField.tokenPositions.stream().filter(t -> t.owner == otherPlayers.get(0)).count();
		rating -= evaFunc.b * currentField.tokenPositions.stream().filter(t -> t.owner == otherPlayers.get(1)).count();
		// Scores
		rating += evaFunc.c * currentField.scores[playerNumber];
		rating -= evaFunc.d * currentField.scores[otherPlayers.get(0)];
		rating -= evaFunc.d * currentField.scores[otherPlayers.get(1)];
		// Winning / losing
		if (currentField.scores[playerNumber] == 5) {
			rating += Integer.MAX_VALUE; // you would win
		}
		if (currentField.scores[otherPlayers.get(0)] == 5 || currentField.scores[otherPlayers.get(1)] == 5) {
			rating -= Integer.MIN_VALUE; // you would lose
		}

		// TODO

		return rating;

	}

}
