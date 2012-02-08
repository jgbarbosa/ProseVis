package data;

public class NodeIterator {
	
	
	public boolean startInd = true;
	
	public WordNode currWord;
	
	public NodeIterator(HierNode currNode){
		setDisplayBreak(currNode);
		
		while(!currNode.isPreWord()){
			currNode = (HierNode)currNode.getFirstChild();
		}
		
		currWord = (WordNode)currNode.getFirstChild();
	}
	
	public void setDisplayBreak(HierNode currNode){
		
		while(!currNode.isPreWord()){
			currNode = (HierNode)currNode.getLastChild();
		}
			
		WordNode lastWord = (WordNode)currNode.getLastChild();
		lastWord.setDisplayBreak(true);
			
	}
	
	public boolean breakNext(){
		
		if(currWord == null){
			return false;
		}
		
		if(currWord.getDisplayBreak()){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean punctNext(){
		
		if(currWord.getNext() == null){
			return false;
		}
		
		if(((WordNode)currWord.getNext()).isPunct()){
			return true;
		}else{
			return false;
		}
	}
	
	public WordNode next(){
		
		if(currWord.getDisplayBreak()){
			currWord.setDisplayBreak(false);
			return null;
		}
		
		if(startInd){
			startInd = false;
		}else{
			currWord = (WordNode)currWord.getNext();
		}
		
		return currWord;
	}
}
