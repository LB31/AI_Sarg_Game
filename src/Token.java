public class Token implements Comparable<Token>  {
	public int x;
    public int y;
    public int owner;
    public boolean mine;
    public int fieldsToGo; // shortest way to a point
    
    public Token(int x, int y) {
    	this.x = x;
    	this.y = y;
    }
    
    public Token(int x, int y, int owner, int mainPlayer) {
    	this.x = x;
    	this.y = y;
    	this.owner = owner;
    	if(owner == mainPlayer)
    		mine = true;
    }
    
    // clone constructor
    public Token(Token token) {
    	this.x = token.x;
    	this.y = token.y;
    	this.owner = token.owner;
    	this.mine = token.mine;
    }
    

	@Override
	public int compareTo(Token o) {
		return Integer.compare(this.fieldsToGo, o.fieldsToGo);
	}
	
	public int getFieldsToGo() {
		return fieldsToGo;
	}
	
    
}
