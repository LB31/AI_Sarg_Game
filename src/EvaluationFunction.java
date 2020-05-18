
public class EvaluationFunction {
	// Token amounts
	public float a; // tokenAmountOwn
	public float b; // tokenAmountPlayer2
	public float c; // tokenAmountPlayer3
	
	// Scores
	public float d; // scoreOwn
	public float e; // scrorePlayer2
	public float f; // scorePlayer3
	
	// Tokens in a row
	public float g; // inRowOwn
	public float h; // inRowPlayer2
	public float i; // inRowPlayer3
	
	// Distance to score
	public float j; // only own

	public EvaluationFunction(float a, float b, float c, float d, float e, float f, float g, float h, float i,
			float j) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
		this.g = g;
		this.h = h;
		this.i = i;
		this.j = j;
	}
	

	
	
	
	
	
}
