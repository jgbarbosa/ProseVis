package data;

import java.util.ArrayList;

public class HierNode extends ProseNode{
	
	int phonemeCount;
	int wordCount;
	double textWidth;
	double phonemeWidth;
	double posWidth;
	
	boolean preWord;
	ArrayList<ProseNode> children;
	
	public HierNode(boolean preWord){
		this.preWord = preWord;
		
		children = new ArrayList<ProseNode>();
		phonemeCount = 0;
		wordCount = 0;
		textWidth = 0.0;
		phonemeWidth = 0.0;
		posWidth = 0.0;
	}
	
	public void incTextWidth(double incWidth){
		textWidth += incWidth;
	}
	
	public double getTextWidth(){
		return textWidth;
	}
	
	public void incPhonemeWidth(double incWidth){
		phonemeWidth += incWidth;
	}
	
	public double getPhonemeWidth(){
		return phonemeWidth;
	}
	
	public void incPOSWidth(double incWidth){
		posWidth += incWidth;
	}
	
	public double getPOSWidth(){
		return posWidth;
	}
	
	public void incWordCount(){
		wordCount++;
	}
	
	public int getWordCount(){
		return wordCount;
	}
	
	public void incPhonemeCount(){
		phonemeCount++;
	}
	
	public int getPhonemeCount(){
		return phonemeCount;
	}
	
	public void addChild(ProseNode child){
		children.add(child);
	}
	
	public boolean isPreWord(){
		return preWord;
	}

	public ArrayList<ProseNode> getChildren(){
		return children;
	}
	
	public ProseNode getFirstChild(){
		
		return children.get(0);
	}
	
	public ProseNode getLastChild(){
		int size = numChildren();
		return children.get(size-1);
	}
	
	public int numChildren(){
		return children.size();
	}
}
