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

	private int timeLimit;
	private int latency;

	private boolean timerHasFinished;

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

	private int searchDepth = 8;

	private Token bestTokenToMove;
	
	private int pruningCounter;
	
	private boolean gameOver;

	public ClientSarg(String name, EvaluationFunction eva) throws IOException {
		playerName = name;
		evaFunc = eva;
	}
	
	public int getPlayerNumber() {
		return playerNumber;
	}
	
	public int[] getScores() {
		return mainField.scores;
	}

	private void initialize() {
		playerNumber = nc.getMyPlayerNumber(); // 0 = red, 1 = green, 2 = blue
		otherPlayers.remove(playerNumber);
		if (playerNumber == 1) { // to keep the right order for alpha-beta pruning
			otherPlayers.set(0, 2);
			otherPlayers.set(1, 0);
		}

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

			timeLimit = nc.getTimeLimitInSeconds();
			latency = nc.getExpectedNetworkLatencyInMilliseconds();

		} catch (IOException e) {
			e.printStackTrace();
		}

		update();
	}

	private void update() {
		while (!gameOver) {
			// Catch the end of the game
			if(Arrays.stream(mainField.scores).anyMatch(i -> i == 5)) {
//				System.out.println(Arrays.toString(mainField.scores));
				gameOver = true;
				return;
			}
			Move receivedMove = nc.receiveMove();
			if (receivedMove == null) {
				// my move
				AlarmClockThread act = new AlarmClockThread(timeLimit, latency, this);
				Move move = calculateMove();
				if (!timerHasFinished && move != null) {
					act.timer.cancel();
					nc.sendMove(move);
					bestTokenToMove = null;
				}
			} else {
				updatePlayfield(receivedMove, false);
				timerHasFinished = false;
			}
		}
	}

	public void sendMove() {
		timerHasFinished = true;
		Token token = bestTokenToMove;
		if(token == null) {
			token = emergencyToken(); 
		}
		nc.sendMove(new Move(token.x, token.y));
		bestTokenToMove = null;
	}

	private Move calculateMove() {
		Move move = null;

		alphaBetaFields.push(newPlayingFieldCopy(mainField));
		AlphaBeta(searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
		alphaBetaFields.empty();

		Token token = bestTokenToMove;

		// when no move was found just pick one
		if (token == null) {
			token = emergencyToken();
		}

		move = new Move(token.x, token.y);

		return move;
	}

	private Token emergencyToken() {
		Token token;
		Token[] myTokens = mainField.tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);
		Arrays.sort(myTokens, Comparator.comparing(Token::getFieldsToGo));
		if(myTokens.length == 0) return null;
		token = myTokens[0];
		System.out.println("EMERGENCY");
		return token;
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

	public void updatePlayfield(Move move, boolean alphaBeta) {
		PlayingField playingField;

		if (alphaBeta) {
			// create a new playing field with the stats from the last move
			playingField = newPlayingFieldCopy(alphaBetaFields.peek());
		} else { // for the actual updating of the original field
			playingField = mainField;
		}

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
	 * @param player: 1 = max, 2 = min, 3 = min
	 */
	private float AlphaBeta(int depth, float alpha, float beta, int player) {
		if (depth == 0) {
			return calculateRating(alphaBetaFields.peek());
		}

		if (player == 1) {
			float maxEval = alpha;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream().filter(t -> t.mine)
					.toArray(Token[]::new);
			// start with Tokens with the shortest way to go, to find the best move faster
			Arrays.sort(currentTokens, Comparator.comparing(Token::getFieldsToGo));
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				float eval = AlphaBeta(depth - 1, maxEval, beta, 2);
				alphaBetaFields.pop(); // undo move
				if (eval > maxEval) {
					maxEval = eval;
					if (depth == searchDepth) {
						bestTokenToMove = token;
					}
					if (maxEval >= beta) {
						pruningCounter++;
						break; // pruning
					}
				}
			}

			return maxEval;

		} else if (player == 2) {
			float minEval = beta;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream()
					.filter(t -> t.owner == otherPlayers.get(0)).toArray(Token[]::new); // get is hard coded here
			// start with Tokens with the shortest way to go, to find the best move faster
			Arrays.sort(currentTokens, Comparator.comparing(Token::getFieldsToGo));
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				float eval = AlphaBeta(depth - 1, alpha, minEval * 0.5f, 3);
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
			float minEval = beta;
			Token[] currentTokens = alphaBetaFields.peek().tokenPositions.stream()
					.filter(t -> t.owner == otherPlayers.get(1)).toArray(Token[]::new); // get is hard coded here
			// start with Tokens with the shortest way to go, to find the best move faster
			Arrays.sort(currentTokens, Comparator.comparing(Token::getFieldsToGo));
			for (Token token : currentTokens) {
				updatePlayfield(new Move(token.x, token.y), true); // make move
				float eval = AlphaBeta(depth - 1, alpha, minEval * 0.5f, 1);
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

	private float calculateRating(PlayingField currentField) {
		float rating = 0;

		// Winning / losing
		if (currentField.scores[playerNumber] == 5) {
			return 100000; // you would win
		}
		if (currentField.scores[otherPlayers.get(0)] == 5 || currentField.scores[otherPlayers.get(1)] == 5) {
			return -100000; // you would lose
		}

		// Token amounts
		rating += evaFunc.cs[0] * currentField.tokenPositions.stream().filter(t -> t.mine).count();
		rating -= evaFunc.cs[1] * currentField.tokenPositions.stream().filter(t -> t.owner == otherPlayers.get(0)).count();
		rating -= evaFunc.cs[1] * currentField.tokenPositions.stream().filter(t -> t.owner == otherPlayers.get(1)).count();
		// Scores
		rating += evaFunc.cs[2] * currentField.scores[playerNumber];
		rating -= evaFunc.cs[3] * currentField.scores[otherPlayers.get(0)];
		rating -= evaFunc.cs[3] * currentField.scores[otherPlayers.get(1)];
		// Distances to make a point
		int distanceToWin = 0;
		Token[] myTokens = currentField.tokenPositions.stream().filter(t -> t.mine).toArray(Token[]::new);
		for (Token token : myTokens) {
			distanceToWin += token.fieldsToGo;
		}
		rating -= evaFunc.cs[4] * distanceToWin;
		return rating;
	}
	
	// For the final fight
	public static void main(String[] args) throws IOException {
		ClientSarg cs = new ClientSarg("Leonid", new EvaluationFunction(new float[] {9.2404995f, 0.67375f, 9.08125f, 7.2682505f, 0.341775f}));
		new Thread(cs).start();
	}
	
}
