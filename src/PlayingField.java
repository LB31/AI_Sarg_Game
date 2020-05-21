import java.util.ArrayList;
import java.util.List;

public class PlayingField {

	public Token[][] playfield = new Token[9][9];
	public List<Token> tokenPositions = new ArrayList<Token>();
	public int[] scores = new int[3];
	
	public PlayingField(Token[][] playfield, List<Token> tokenPositions, int[] scores) {
		this.playfield = playfield;
		this.tokenPositions = tokenPositions;
		this.scores = scores;
	}
	
	public PlayingField() {};
	
}
