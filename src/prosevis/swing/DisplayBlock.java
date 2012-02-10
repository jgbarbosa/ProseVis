package prosevis.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import prosevis.data.WordNode;

public class DisplayBlock extends JPanel{
	private static final long serialVersionUID = 1L;

	private WordNode wordNode;
	private Visualization viz;
	private VizController ctr;
	private TextLayout textToRender;
	private double tokenWidth;
	private boolean lineLevel;
		
	public DisplayBlock(WordNode node, Visualization viz, VizController ctr, 
			double tokenWidth, TextLayout textToRender){
		this.wordNode = node;
		this.viz = viz;
		this.ctr = ctr;
		this.tokenWidth = tokenWidth;
		this.textToRender = textToRender;
		this.lineLevel = false;
	}

	public DisplayBlock(double tokenWidth, Visualization viz, VizController ctr, TextLayout textToRender) {
		this.viz = viz;
		this.ctr = ctr;
		this.tokenWidth = tokenWidth;
		this.lineLevel = true;
		this.textToRender = textToRender;
	}
	
	public void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		this.setBackground(Color.WHITE);

		double lineHeight = ctr.getMaxHeight(g.getFontRenderContext(), viz.scaledFont) * 1.5; 
		double maxDescent = ctr.getMaxDescent(g.getFontRenderContext(), viz.scaledFont);

		double yPos = lineHeight - maxDescent;

		// Render lineLevel
		if (this.lineLevel) {
			g.setPaint(Color.GRAY);
			g.fill(new Rectangle2D.Double(0.0, 0, tokenWidth, lineHeight));
			g.setPaint(Color.WHITE);
			textToRender.draw(g, (float) 0, (float) yPos);
			return;
		}
		
		//Render Color
		else if (ctr.renderColor) {
			//Coloring by Phoneme
			if (ctr.colorVar.equals("Sound") || ctr.colorVar.equals("Stress")) {
				int phonemeCount = wordNode.getSyllableCount();
				double phonemeWidth;

				this.setBackground(ctr.getCurColor(wordNode, 0));

				if(ctr.renderText){
					phonemeWidth = tokenWidth / (double)phonemeCount;
				}else{
					phonemeWidth = ctr.zoom * 10.0;
				}

				double cxPos = phonemeWidth;

				for(int i = 1; i < phonemeCount; i++){
					Color unitColor = ctr.getCurColor(wordNode, i);

					g.setPaint(unitColor);
					g.fill(new Rectangle2D.Double(cxPos, 0, phonemeWidth, lineHeight));

					cxPos += phonemeWidth;
				}
				//Coloring by Word
			} else{
				this.setBackground(ctr.getCurColor(wordNode, 0));
			}
			//Do NOT Render Color
		}else{

			//Render tone but not color - white background
			if(ctr.renderTone && !wordNode.isPunct()){
				this.setBackground(Color.LIGHT_GRAY);
			}

		}
		
		if(ctr.searchInd){
			if(ctr.searchMatch(wordNode)){
				this.setBackground(Color.YELLOW);
			}
		}
		
		//Render accent by re-drawing the negative space
		if(ctr.renderTone){

			String accent = viz.getFile().getAccentText(wordNode.getAccent());

			ArrayList<Integer> rawxPts = ctr.xAccPts.get(accent);
			ArrayList<Integer> rawyPts = ctr.yAccPts.get(accent);
			Path2D.Double polyPoints = new Path2D.Double();

			polyPoints.moveTo(tokenWidth, 0);
			polyPoints.lineTo(0, 0);

			double xCoor;
			double yCoor;

			for (int i = 2; i < rawxPts.size(); i++) {
				xCoor = ((double)rawxPts.get(i)/5.0 * tokenWidth);
				yCoor = (yPos + maxDescent - (((double)rawyPts.get(i))/5.0 * lineHeight));

				polyPoints.lineTo(xCoor, yCoor);
			}

			polyPoints.lineTo(tokenWidth, 0);

			g.setPaint(Color.WHITE);
			g.fill(new Rectangle2D.Double(0, 0, tokenWidth, 1));
			g.fill(polyPoints);

		}

		// Render by David's data
		if (ctr.renderDdata) {
	
			int phonemeCount = wordNode.getSyllableCount();
			double phonemeWidth;
			
			this.setBackground(Color.WHITE);
						
			phonemeWidth = tokenWidth / (double)phonemeCount;
			double cxPos = 0.0;

			for(int i = 0; i < phonemeCount; i++){
				Color unitColor;
				float dDataRelProb = wordNode.getDavidDataRelProb(i, ctr.actFiles);
				int dDataIdx = wordNode.getDavidDataProbIdx(i, ctr.actFiles);
				
				if (dDataIdx != -1) {
					unitColor = viz.getDavidDataColor(dDataIdx);
					int[] compArray = new int[3];
					compArray[0] = unitColor.getRed();
					compArray[1] = unitColor.getGreen();
					compArray[2] = unitColor.getBlue();
					float[] hsbvals = new float[3];
					Color.RGBtoHSB(compArray[0], compArray[1], compArray[2], hsbvals);
					unitColor = Color.getHSBColor(hsbvals[0], dDataRelProb, hsbvals[2]);
				}
				else
					unitColor = Color.WHITE;
				 		
				g.setPaint(unitColor);
				g.fill(new Rectangle2D.Double(cxPos, 0, phonemeWidth, lineHeight));

				cxPos += phonemeWidth;
			}
		}
				
		//Render the text
		if (ctr.renderText) {
			g.setPaint(Color.BLACK);
			textToRender.draw(g, (float) 0, (float) yPos);
		}
	}
}