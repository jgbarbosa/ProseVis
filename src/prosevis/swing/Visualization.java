package prosevis.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JViewport;

import prosevis.data.HierNode;
import prosevis.data.InputFile;
import prosevis.data.NodeIterator;
import prosevis.data.WordNode;

@SuppressWarnings("serial")
public class Visualization extends JPanel implements ComponentListener {
  private final Color davidDataColor[] = { Color.RED, Color.BLUE, Color.GREEN,
      Color.ORANGE, Color.GRAY, Color.YELLOW, Color.CYAN, Color.MAGENTA };
  private VizController ctr;

  // Attributes of the visualization area
  public Rectangle viewArea;

  // Files being displayed
  private boolean open;
  private InputFile file;
  private boolean viewLineNumber = false;

  // Navigation display
  public BufferedImage vizCache;

  // Text font
  public Font scaledFont;

  public Visualization() {
    ctr = null;

    // Initialize rendering options
    scaledFont = new Font("Helvetica", Font.PLAIN, 12);
    vizCache = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
  }

  public Color getDavidDataColor(int index) {
    return this.davidDataColor[index];
  }

  public void setScaledFont(Font newFont) {
    this.scaledFont = newFont;
  }

  public void setController(VizController ctr) {
    this.ctr = ctr;
  }

  /* This function will draw out the color for a whole document, line by line */
  private void drawColumnColor(Graphics2D g) {
    this.removeAll();

    double lineHeight = ctr.getMaxHeight(g.getFontRenderContext(), scaledFont) * 1.5;
    double maxDescent = ctr.getMaxDescent(g.getFontRenderContext(), scaledFont);

    double yPos = lineHeight - maxDescent;

    // For each line in the column
    HierNode lineNode = file.firstElements.get(ctr.lineLevel);

    int count = -1;
    while (lineNode != null) {
      count++;
      double xPos = 1.0;

      if (viewLineNumber == true) {
        // Display the corresponding level number, ie, line number or paragraph
        // no. or section no, etc
        TextLayout lineLevelText = new TextLayout(count + " ", scaledFont,
            g.getFontRenderContext());
        double levelWidth = lineLevelText.getAdvance();
        DisplayBlock displayLevelBlock = new DisplayBlock(levelWidth, this,
            this.ctr, lineLevelText);
        displayLevelBlock.setBounds(new Rectangle(0,
            (int) (yPos - lineHeight), (int) levelWidth, (int) lineHeight));
        displayLevelBlock.setBorder(BorderFactory.createEtchedBorder());
        this.add(displayLevelBlock);
        xPos += levelWidth;
      }

      // For each token in the line
      NodeIterator words = new NodeIterator(lineNode);
      WordNode wordNode = words.next();

      Rectangle2D lineArea = new Rectangle2D.Double(xPos, yPos - lineHeight,
          ctr.vizWidth, lineHeight * 2);

      if (lineArea.intersects(viewArea)) {

        while (wordNode != null) {

          double tokenWidth = 0.0;
          TextLayout textToRender = null;
          double cyPos = yPos + maxDescent - lineHeight;

          // Compute width of the word
          if (ctr.renderText) {
            String text = null;

            if (ctr.textRenderVar == 0) {
              text = wordNode.getWord();
            } else if (ctr.textRenderVar == 1) {
              text = file.reconstructPhoneme(wordNode);
              /*
               * Punctuation: Instead of NULL, display the word.
               */
              if (text.contains("NULL"))
                text = wordNode.getWord();
            } else {
              text = wordNode.getPOS().toString().toLowerCase();
            }

            textToRender = new TextLayout(text + " ", scaledFont,
                g.getFontRenderContext());
            tokenWidth = textToRender.getAdvance();
          } else {
            tokenWidth = wordNode.getSyllableCount() * (ctr.zoom * 10.0);
          }

          DisplayBlock block = new DisplayBlock(wordNode, this, this.ctr,
              tokenWidth, textToRender);
          block.setBounds(new Rectangle((int) xPos, (int) cyPos,
              (int) tokenWidth, (int) lineHeight));

          // Generate tooltip
          if (ctr.displayTooltip) {
            if (ctr.tooltipVar == 1) {
              block.setToolTipText(wordNode.getPOS().toString().toLowerCase());
            } else if (ctr.tooltipVar == 2) {
              block.setToolTipText(file.reconstructPhonemeT(wordNode));
            } else {
              block.setToolTipText(wordNode.getWord());
            }
          }

          if (!ctr.renderText) {
            block.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          }

          this.add(block);

          xPos += tokenWidth;
          wordNode = words.next();
        }
      }
      /*
       * Display blank lines between consecutive rendered lines
       *
       * yPos += lineHeight; // Leave a blank line TextLayout blankLineText =
       * new TextLayout(" ", scaledFont, g.getFontRenderContext()); DisplayBlock
       * displayBlankLineBlock = new DisplayBlock(levelWidth, this, this.ctr,
       * blankLineText); displayBlankLineBlock.setBounds(new Rectangle((int)0,
       * (int)(yPos-lineHeight), (int)levelWidth, (int)lineHeight));
       * displayBlankLineBlock.setBorder(BorderFactory.createEtchedBorder());
       * this.add(displayBlankLineBlock);
       */

      yPos += lineHeight;
      lineNode = (HierNode) lineNode.getNext();
    }
  }

  /* Add a file to the visualization */
  public void setFile(InputFile file) {
    this.file = file;
    this.open = true;
  }

  /* Returns the last file added to the visualization */
  public InputFile getFile() {
    return file;
  }

  /* Returns whether there are any files to display */
  public boolean openFile() {
    return open;
  }

  /* Close the file in the visualization */
  public void closeFile() {
    open = false;
    this.repaint();
  }

  /* PAINT PAINT PAINT */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    setBackground(Color.white);

    if (open && ctr != null) {
      JViewport viewPort = (JViewport) this.getParent();
      viewArea = viewPort.getViewRect();

      // Draw color and text
      drawColumnColor((Graphics2D) g);

      ctr.nav.repaint();
    }
  }

  @Override
  public void componentHidden(ComponentEvent e) {
  }

  @Override
  public void componentMoved(ComponentEvent e) {
  }

  @Override
  public void componentShown(ComponentEvent e) {
  }

  /* Reset size attributes */
  @Override
  public void componentResized(ComponentEvent e) {
    ctr.resetDivider();
  }

  /* Determine integer representation from String */
  public int findIndex(String term, ArrayList<String> list) {
    for (int i = 0; i < list.size(); i++) {
      String compStr = list.get(i).toLowerCase();

      if (compStr.equals(term)) {
        return i;
      }
    }

    return -1;
  }

  public void setViewLineNumber(boolean flag) {
    // TODO Auto-generated method stub
    viewLineNumber = flag;
  }
}