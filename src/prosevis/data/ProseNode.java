package prosevis.data;

public class ProseNode {
	boolean levelBreak = false;
	ProseNode next = null;
	
	public void addBreak(){
		levelBreak = true;
	}
	
	public boolean getBreak(){
		return levelBreak;
	}
	
	public void setNext(ProseNode next){
		this.next = next;
	}
	
	public ProseNode getNext(){
		return next;
	}
}
