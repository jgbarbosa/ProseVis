package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

@SuppressWarnings("serial")
public class Navigator extends JPanel {
	private VizController ctr;

	public Navigator(VizController source)  {
		ctr = source;
	}

	public void paintVizNav(Graphics2D g, int index, Rectangle rect){
		double x, y, w, h;
		Rectangle view = ctr.vizzes[index].viewArea;
		Dimension vizDim = ctr.vizzes[index].getPreferredSize();
		
		if(view == null || vizDim == null){
			return;
		}

		x = (view.getMinX() * rect.width / vizDim.width) + rect.x;
		y = (view.getMinY() * rect.height / vizDim.height) + rect.y;

		w = Math.min(view.getWidth(), vizDim.width) * rect.width / vizDim.width;
		h = Math.min(view.getHeight(), vizDim.height) * rect.height / vizDim.height;


		g.setColor(Color.RED);
		g.draw(new Rectangle2D.Double(x, y, w, h));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);

		if(ctr.open[0] && ctr.open[1]){
			if(ctr.getOrientation() == JSplitPane.HORIZONTAL_SPLIT){
				paintVizNav((Graphics2D)g, 0, new Rectangle(1 , 1,(this.getWidth()/2)-2, this.getHeight()-3));
				paintVizNav((Graphics2D)g, 1, new Rectangle((this.getWidth()/2)+1 , 1,(this.getWidth()/2)-2, this.getHeight()-3));
			}else{
				paintVizNav((Graphics2D)g, 0, new Rectangle(1 , 1,this.getWidth()-3, (this.getHeight()/2)-2));
				paintVizNav((Graphics2D)g, 1, new Rectangle(1, this.getHeight()/2,this.getWidth()-3, (this.getHeight()/2)-2));
			}

			//Draw divider line down the middle
			g.setColor(Color.BLACK);
			if(ctr.getOrientation() == JSplitPane.HORIZONTAL_SPLIT){
				g.drawLine(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight());
			}else{
				g.drawLine(0, this.getHeight()/2, this.getWidth(), this.getHeight()/2);
			}
		}else{
			if(ctr.open[0]){
				paintVizNav((Graphics2D)g, 0, new Rectangle(1, 1,this.getWidth()-3, this.getHeight()-3));
			}

			if(ctr.open[1]){
				paintVizNav((Graphics2D)g, 1, new Rectangle(1, 1,this.getWidth()-3, this.getHeight()-3));
			}
		}
	}
}
