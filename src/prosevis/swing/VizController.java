package prosevis.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;

import prosevis.data.HierNode;
import prosevis.data.ICon;
import prosevis.data.InputFile;
import prosevis.data.NodeIterator;
import prosevis.data.POSType;
import prosevis.data.Syllable;
import prosevis.data.WordNode;


/*
 * This class controls the two visualization panels, keeping track of
 * universal variables such as accent shapes, render options, and font.
 */
public class VizController extends JSplitPane implements ComponentListener{
	private static final long serialVersionUID = -7142290362614932354L;

	//Visualization Panels
	public JScrollPane[] panels = new JScrollPane[2];
	public Visualization[] vizzes = new Visualization[2];

	//Attributes of the visualization area
	double zoom;
	double vizWidth;
	double vizHeight;
	double myWidth;
	double myHeight;

	//Templates containing the tone shapes
	public HashMap<String, ArrayList<Integer>> xAccPts;
	public HashMap<String, ArrayList<Integer>> yAccPts;

	//Files being displayed
	public boolean[] open = new boolean[2];
	private final HashMap<String, InputFile> files;
	private InputFile lastInput;

	//Color variables
	public ArrayList<Color> curColors;
	public String colorVar;
	public String colorPVar;

	//Rendering Options
	public boolean renderText;
	public int textRenderVar;
	public boolean renderTone;
	public boolean renderColor;
	public boolean tileHorizontal;
	public boolean searchInd;
	public boolean renderDdata;

	//Tooltip variable
	boolean displayTooltip;
	int tooltipVar;

	//Search variables
	int searchTerm;
	String searchStr;
	int searchVar;
	int searchPVar;
	int searchCount;
	int lastPosition;
	public ArrayList<Point> lineNumbers;
	JScrollPane lastPane;

	//Level of tree where line breaks occur
	int lineLevel = 4;

	//Text font
	private Font gFont;
	private Font scaledFont;

	//Navigator
	public Navigator nav;

	// David's data active files
	boolean[] actFiles = new boolean[ICon.MAX_PROB];

	int maxLines;
	double totalHeight;
	double maxLineAdvance;

	public VizController(int orientation, JScrollPane leftComponent, JScrollPane rightComponent,
			Visualization leftViz, Visualization rightViz) {
		super(orientation, leftComponent, rightComponent);

		//Initialize visualization objects in the split pane
		panels[0] = leftComponent;
		panels[1] = rightComponent;

		vizzes[0] = leftViz;
		vizzes[1] = rightViz;

		vizzes[0].setController(this);
		vizzes[1].setController(this);

		//Initialize rendering options
		zoom = 1;

		gFont = new Font("Helvetica", Font.PLAIN, 2);
		renderDdata = false;
		renderText = true;
		textRenderVar = 0;
		renderTone = false;
		renderColor = false;
		tileHorizontal = true;
		searchInd = false;
		searchCount = -1;
		lastPosition = -1;
		searchStr = null;
		colorVar = "None";
		colorPVar = "Full";
		displayTooltip = false;
		tooltipVar = 0;

		curColors = new ArrayList<Color>();
		lineNumbers = new ArrayList<Point>();
		lastPane = null;

		yAccPts = new HashMap<String, ArrayList<Integer>>();
		xAccPts = new HashMap<String, ArrayList<Integer>>();

		//Accent Shapes
		Integer[] nX = {5, 0, 0, 5};
		Integer[] nY = {5, 5, 3, 3};
		xAccPts.put("", new ArrayList<Integer>(Arrays.asList(nX)));
		yAccPts.put("", new ArrayList<Integer>(Arrays.asList(nY)));
		xAccPts.put("NULL", new ArrayList<Integer>(Arrays.asList(nX)));
		yAccPts.put("NULL", new ArrayList<Integer>(Arrays.asList(nY)));

		Integer[] lX = {5, 0, 0, 5};
		Integer[] lY = {5, 5, 2, 2};
		xAccPts.put("L*", new ArrayList<Integer>(Arrays.asList(lX)));
		yAccPts.put("L*", new ArrayList<Integer>(Arrays.asList(lY)));

		Integer[] hX = {5, 0, 0, 5};
		Integer[] hY = {5, 5, 4, 4};
		xAccPts.put("H*", new ArrayList<Integer>(Arrays.asList(hX)));
		yAccPts.put("H*", new ArrayList<Integer>(Arrays.asList(hY)));

		Integer[] HX = {5, 0, 0, 2, 3, 5};
		Integer[] HY = {5, 5, 4, 3, 4, 3};
		xAccPts.put("!H*", new ArrayList<Integer>(Arrays.asList(HX)));
		yAccPts.put("!H*", new ArrayList<Integer>(Arrays.asList(HY)));

		Integer[] lHX = {5, 0, 0, 1, 3, 5};
		Integer[] lHY = {5, 5, 3, 2, 4, 2};
		xAccPts.put("L+H*", new ArrayList<Integer>(Arrays.asList(lHX)));
		yAccPts.put("L+H*", new ArrayList<Integer>(Arrays.asList(lHY)));

		Integer[] LhX = {5, 0, 0, 3, 5};
		Integer[] LhY = {5, 5, 2, 3, 4};
		xAccPts.put("L*+H", new ArrayList<Integer>(Arrays.asList(LhX)));
		yAccPts.put("L*+H", new ArrayList<Integer>(Arrays.asList(LhY)));

		files = new HashMap<String, InputFile>();

		for (int i = 0; i < ICon.MAX_PROB; i++)
			actFiles[i] = true;
	}

	/* Current font in use */
	public Font getSFont(){
		return scaledFont;
	}

	/* Set pointer to the navigation panel */
	public void setNav(Navigator nav){
		this.nav = nav;
	}

	/* Add a file to the visualization */
	public void addFile(InputFile file) {
		JMenuBar nameBar = new JMenuBar();
		JMenu name = new JMenu(file.getName());
		nameBar.add(name);

		if(!open[0]){
			vizzes[0].setFile(file);
			open[0] = true;
			panels[0].setColumnHeaderView(nameBar);
			lastPane = panels[0];
		}else{
			vizzes[1].setFile(file);
			open[1] = true;
			panels[1].setColumnHeaderView(nameBar);
			lastPane = panels[1];
		}

		files.put(file.getName(), file);
		lastInput = file;
		resetFileSetup();

		if(renderColor){
			generateColors();
		}
	}

	/* Returns the last file added to the visualization */
	public InputFile getFile(){
		return lastInput;
	}

	/* Returns whether there are any files to display */
	public boolean noFiles(){
		return files.isEmpty();
	}

	/* Close all the files in the visualization */
	public void closeFiles() {
		files.clear();

		for(int i = 0; i < open.length; i++){
			if (open[i] == true) {
				open[i] = false;
				panels[i].getColumnHeader().invalidate();
				panels[i].setColumnHeader(null);
				PVToolPanel.removeFileItem(vizzes[i].getFile().getName());
				vizzes[i].closeFile();
			}
		}
		resetFileSetup();
	}

	/* Close an individual file */
	public void closeFile(String fileName) {
		files.remove(fileName);

		for(int i = 0; i < open.length; i++){
			if(open[i]){
				if(vizzes[i].getFile().getName().equals(fileName)){
					open[i] = false;
					panels[i].getColumnHeader().invalidate();
					panels[i].setColumnHeader(null);
					vizzes[i].closeFile();
				}
			}
		}

		resetFileSetup();
	}

	/* Reset the divider location and last input */
	public void resetFileSetup(){
		if(open[0] && open[1]){
			this.setDividerLocation(.5);
		}else{
			if(open[1]){
				this.setDividerLocation(0.0);
				lastInput = vizzes[1].getFile();
				lastPane = panels[1];
			}else{
				this.setDividerLocation(1.0);
				lastInput = vizzes[0].getFile();
				lastPane = panels[0];
			}
		}
	}

	/* Reset the divider location */
	public void resetDivider(){
		if(!(open[0] && open[1])){
			if(open[1]){
				this.setDividerLocation(0.0);
			}else{
				this.setDividerLocation(1.0);
			}
		}
	}

	/* Font ascent given the current settings */
	public double getMaxAscent(FontRenderContext frc, Font font){
		String text = "N";
		TextLayout layoutData = new TextLayout(text, font, frc);

		return layoutData.getAscent();
	}

	/* Font descent given the current settings */
	public double getMaxDescent(FontRenderContext frc, Font font){
		String text = "g";
		TextLayout layoutData = new TextLayout(text, font, frc);

		return layoutData.getDescent();
	}

	/* Total font height */
	public double getMaxHeight(FontRenderContext frc, Font font){
		return getMaxAscent(frc, font) + getMaxDescent(frc, font);
	}

	public double getMaxWidth(FontRenderContext frc, Font font) {
		String text = "W";
		TextLayout layoutData = new TextLayout(text, font, frc);
		return layoutData.getAdvance() + layoutData.getLeading();
	}

	public boolean isDavidDataSet() {
		for (boolean value : actFiles) {
			if (value)
				return true;
		}
		return false;
	}

	/*
	 * This method performs the layout computations for the two visualizations
	 */
	public void prepViz(Graphics2D g, double vWidth, double vHeight, boolean vizPrep) {
		int maxPhonemes = 0;
		double charAdvance = getMaxWidth(g.getFontRenderContext(), gFont);
		maxLineAdvance = 0;
		double maxLineHeight = getMaxHeight(g.getFontRenderContext(), gFont);
		//double totalWidth = 0;
		totalHeight = 0;
		maxLines = 0;

		//Determine the maximum column width based on the display settings
		for (InputFile file: files.values()) {

			int lineCt = file.getNodeCount(lineLevel);

			if(lineCt > maxLines){
				maxLines = lineCt;
			}

			int phonemeCt = file.getMaxPhonemes(lineLevel);
			if(phonemeCt > maxPhonemes){
				maxPhonemes = phonemeCt;
			}

			double lineAdv = 0;

			if(textRenderVar == 0){
				lineAdv = file.getMaxWordWidth(lineLevel);
			}else if(textRenderVar == 1){
				lineAdv = file.getMaxPhonemeWidth(lineLevel);
			}else{
				lineAdv = file.getMaxPOSWidth(lineLevel);
			}

			if(lineAdv > maxLineAdvance){
				maxLineAdvance = lineAdv;
			}
		}

		//Provides room for the tone rendering
		maxLineHeight *= 1.5;

		//Determine total width and height
		totalHeight = maxLineHeight * maxLines;
		totalHeight += 8;

/*
		totalWidth = maxLineAdvance;
		totalWidth += 8;

		double xScale = (totalWidth + 4) / vWidth;
		double yScale = (totalHeight + 4) / vHeight;
		double scale;

		if (xScale < yScale) {
			scale = xScale;
		} else {
			scale = yScale;
		}
*/
		//Set the dimensions of each visualization
		for(int i = 0; i < open.length; i++){
			if(open[i]){
				InputFile file = vizzes[i].getFile();
				vizzes[i].setScaledFont(gFont);

				double lineAdv;
				if(textRenderVar == 0){
					lineAdv = file.getMaxWordWidth(lineLevel);
				}else if(textRenderVar == 1){
					lineAdv = file.getMaxPhonemeWidth(lineLevel);
				}else{
					lineAdv = file.getMaxPOSWidth(lineLevel);
				}

				int prefWidth = (int)(lineAdv * charAdvance + 12);
				int prefHeight = (int)(file.getNodeCount(lineLevel) * maxLineHeight + 12);

				vizzes[i].setPreferredSize(new Dimension(prefWidth, prefHeight));
				vizzes[i].revalidate();
			}
		}
	}

	/* PAINT  PAINT  PAINT */
	@Override
  public void paintComponent(Graphics g) {

		if (!files.isEmpty()) {
			prepViz((Graphics2D) g, vizWidth, vizHeight, true);
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
		myWidth = this.getWidth();
		myHeight = this.getHeight();
		vizWidth = zoom * myWidth;
		vizHeight = zoom * myHeight;
		AffineTransform scaleT = new AffineTransform();
		scaleT.setToScale(zoom, zoom);
		gFont = gFont.deriveFont(scaleT);
	}

	/* Returns the display color of a given WordNode */
	public Color getCurColor(WordNode wordNode, int i){

		if(wordNode.isPunct()){
			return Color.LIGHT_GRAY;
		}

		if(colorVar.equals("Sound")){
			if(colorPVar.equals("Full")){
				return curColors.get(wordNode.getSyllables().get(i).getPhoneme());
			}else if(colorPVar.equals("Beginning")){
				return curColors.get(wordNode.getSyllables().get(i).getComponent(0));
			}else if(colorPVar.equals("Vowel")){
				return curColors.get(wordNode.getSyllables().get(i).getComponent(1));
			}else{
				return curColors.get(wordNode.getSyllables().get(i).getComponent(2));
			}
		}else if(colorVar.equals("Stress")){
			return curColors.get(wordNode.getSyllables().get(i).getStress());
		}else if(colorVar.equals("POS")){
			return curColors.get(wordNode.getPOS().ordinal());
		}else if(colorVar.equals("Accent")){
			return curColors.get(wordNode.getAccent());
		}else if(colorVar.equals("Tone")){
			return curColors.get(wordNode.getTone());
		}else if(colorVar.equals("Soundex")){
			return curColors.get(wordNode.getSoundex());
		}else if(colorVar.equals("Word")){
			return curColors.get(wordNode.getWrd());
		}

		return curColors.get(0);
	}


	/* Generates the color mappings based on the current color variable */
	public void generateColors(){
		int colorCount = 0;
		curColors.clear();

		if(colorVar.equals("Sound")){
			if(colorPVar.equals("Full")){
				for (InputFile file: files.values()) {
					if(file.phonemeCode.size() > colorCount){
						colorCount = file.phonemeCode.size();
					}
				}
			}else if(colorPVar.equals("Beginning")){
				for (InputFile file: files.values()) {
					if(file.phoC1Code.size() > colorCount){
						colorCount = file.phoC1Code.size();
					}
				}
			}else if(colorPVar.equals("Vowel")){
				for (InputFile file: files.values()) {
					if(file.phoVCode.size() > colorCount){
						colorCount = file.phoVCode.size();
					}
				}
			}else{
				for (InputFile file: files.values()) {
					if(file.phoC2Code.size() > colorCount){
						colorCount = file.phoC2Code.size();
					}
				}
			}
		}else if(colorVar.equals("Stress")){
			colorCount = 3;

		}else if(colorVar.equals("POS")){
          if(POSType.values().length > colorCount){
            colorCount = POSType.values().length;
          }
		}else if(colorVar.equals("Accent")){
			for (InputFile file: files.values()) {
				if(file.getNumAccents() > colorCount){
					colorCount = file.getNumAccents();
				}
			}
		}else if(colorVar.equals("Tone")){
			for (InputFile file: files.values()) {
				if(file.getNumTones() > colorCount){
					colorCount = file.getNumTones();
				}
			}
		}else if(colorVar.equals("Soundex")){
			for (InputFile file: files.values()) {
				if(file.getNumSounds() > colorCount){
					colorCount = file.getNumSounds();
				}
			}
		}else if(colorVar.equals("Word")){
			for (InputFile file: files.values()) {
				if(file.getNumUniqueWords() > colorCount){
					colorCount = file.getNumUniqueWords();
				}
			}
		}
		int stepCount = 30;
		ArrayList<Color> cl = new ArrayList<Color>();
		for(int i = 0; i < stepCount; i++){
			for(int j = i; j < colorCount; j=j+stepCount) {
				Color color = Color.getHSBColor((float) j / (float) colorCount, 0.5f, 0.90f);
				for (Color tempcolor : cl) {
					if (color.equals(tempcolor)) {
						color = tempcolor.darker().brighter();
						break;
					}
				}
				curColors.add(color);
				cl.add(color);
			}
		}
	}

	/* Initialize size of visualizations and divider location */
	public void initializeSizes(){
		myWidth = this.getWidth();
		myHeight = this.getHeight();
		vizWidth = this.getWidth();
		vizHeight = this.getHeight();

		this.setDividerLocation(1.0);
	}

	/* Set the zoom factor */
	public void setZoomFactor(double scale) {
		zoom = scale;
		AffineTransform scaleT = new AffineTransform();
		scaleT.setToScale(scale, scale);
		gFont = gFont.deriveFont(scaleT);
		/*vizWidth = zoom * myWidth;
		vizHeight = zoom * myHeight;*/
		repaint();
	}

	/* Set level of line breaks */
	public void setLineLevel(int level) {
		this.lineLevel = level;
	}

	/* Render either words, phonemes, or pos */
	public void setTextRenderVar(int var) {
		textRenderVar = var;
	}

	/* Render text or not */
	public void setTextRenderPolicy(boolean policy) {
		renderText = policy;
		textRenderVar = 0;
	}

	/* Display tooltips or not */
	public void setTooltipPolicy(int policy){
		if(policy == 0){
			displayTooltip = false;
		}else{
			displayTooltip = true;
		}

		tooltipVar = policy;
	}

	/* Tile vertical or horizontal */
	public void setTileRenderPolicy(boolean policy) {
		tileHorizontal = policy;
	}

	/* Render tone or not */
	public void setToneRenderPolicy(boolean policy) {
		renderTone = policy;
	}

	/* Set color variables and make call to generate colors */
	public void setColorRenderPolicy(String colorVar) {
		this.colorVar = colorVar;

		if(colorVar.equals("None")){
			renderColor = false;
		}else{
			renderColor = true;
			generateColors();
		}
	}

	/* Set color variables at the phoneme level */
	public void setPhonemeRenderPolicy(String phonemeVar) {
		this.colorPVar = phonemeVar;

		generateColors();
	}

	/* Number of files being displayed (0, 1, or 2) */
	public int getNumFiles(){
		return files.size();
	}

	/* Prep to display search terms */
	public void setSearch(String term, int searchVar, int phoVar){
		if(files.size() == 0){
			return;
		}

		if (searchStr != null && searchStr.equals(term)) {
			if (searchCount != -1 && (searchCount < lineNumbers.size() - 1)) {
				searchCount++;
				searchDisplay(searchCount);
				return;
			} else if (searchCount != -1 && (searchCount == lineNumbers.size() - 1)) {
				String message = "Search continuing from start of the file";
				JOptionPane.showMessageDialog(new JFrame(), message, "Information", JOptionPane.INFORMATION_MESSAGE);
				searchCount = 0;
				searchDisplay(searchCount);
				return;
			}
		}

		searchStr = term;
		lineNumbers.clear();
		if(searchVar == 0){
			searchTerm = lastInput.getWordId(term.toLowerCase());
		}else if(searchVar == 1){
			if(phoVar == 0){
				searchTerm = findIndex(term, lastInput.phonemeCode);
			}else if(phoVar == 1){
				searchTerm = findIndex(term, lastInput.phoC1Code);
			}else if(phoVar == 2){
				searchTerm = findIndex(term, lastInput.phoVCode);
			}else if(phoVar == 3){
				searchTerm = findIndex(term, lastInput.phoC2Code);
			}
		}else if(searchVar == 2){
			searchTerm = POSType.fromString(term).ordinal();
		}else if(searchVar == 3){
			searchTerm = lastInput.getSoundexId(term);
		}

		if(searchTerm != -1){
			searchInd = true;
			this.searchVar = searchVar;
			this.searchPVar = phoVar;
		}else{
			searchInd = false;
		}

		boolean found = search();
		if (found) {
			searchCount = 0;
			searchDisplay(searchCount);
		}
	}

	/* Checks to see whether the current node matches the current search parameters */
	public boolean searchMatch(WordNode node){
		if(searchVar == 0){
			return (node.getWrd() == searchTerm);
		}else if(searchVar == 1){

			int phonemeCount = node.getSyllableCount();

			for(int i = 0; i < phonemeCount; i++){
				Syllable curSyl = node.getSyllables().get(i);

				if(searchPVar == 0){
					if(searchTerm == curSyl.getPhoneme()){
						return true;
					}
				}else if(searchPVar == 1){
					if(searchTerm == curSyl.getComponent(0)){
						return true;
					}
				}else if(searchPVar == 2){
					if(searchTerm == curSyl.getComponent(1)){
						return true;
					}
				}else if(searchPVar == 3){
					if(searchTerm == curSyl.getComponent(2)){
						return true;
					}
				}
			}

			return false;
		}else if(searchVar == 2){
			return (node.getPOS().ordinal() == searchTerm);
		}else if(searchVar == 3){
			return (node.getSoundex() == searchTerm);
		}

		return false;
	}

	/* Determine integer representation from String */
	public int findIndex(String term, ArrayList<String> list){
		for(int i = 0; i < list.size(); i++){
			String compStr = list.get(i).toLowerCase();

			if(compStr.equals(term)){
				return i;
			}
		}
		return -1;
	}

	/* Turn off search display */
	public void killSearch(){
		searchInd = false;
		lineNumbers.clear();
		searchCount = -1;
	}

	public void searchLine(HierNode lineNode, int number) {
		int count = 0;
		NodeIterator<WordNode> words = new NodeIterator<WordNode>(lineNode);
		WordNode wordNode = words.next();
		while (wordNode != null) {
			if (this.searchMatch(wordNode)) {
			//	wordCount.add(count);
				lineNumbers.add(new Point(number, count));
			}
			count++;
			wordNode = words.next();
		}
		return;
	}

	public boolean search() {
		HierNode lineNode = lastInput.firstElements.get(lineLevel);
		Integer number = -1;
		while(lineNode != null) {
			number++;
			searchLine(lineNode, number);
			lineNode = (HierNode)lineNode.getNext();
		}
		if (lineNumbers.size() == 0) {
			String message = "The search string is not present in the file";
			JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void searchDisplay(int idx) {

		JViewport currView = lastPane.getViewport();
		int newYPosition = (int)((totalHeight / maxLines) * lineNumbers.get(idx).x);
		if (lastPosition == newYPosition)
			return;
		lastPosition = newYPosition;
		currView.setViewPosition(new Point(currView.getViewPosition().x, newYPosition));
	}
}