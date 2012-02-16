package prosevis.processing.model;

import java.util.ArrayList;

import prosevis.data.DataTree;

public class ApplicationModel implements ProseModelIF {

  private ArrayList<DataTreeView> data = new ArrayList<DataTreeView>();
  private int numberRows = 1;
  
  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#addData(prosevis.data.DataTree)
   */
  @Override
  public synchronized void addData(DataTree newTree) {
    String newFilePath = newTree.getPath();
    for (DataTreeView tree: data) {
      if (tree.getData().getPath().equals(newFilePath)) {
        // disallow duplicates
        return;
      }
    }
    data.add(new DataTreeView(newTree));
  }
  
  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#getFileList()
   */
  @Override
  public synchronized ArrayList<String> getFileList() {
    ArrayList<String> ret = new ArrayList<String>();
    for (DataTreeView tree: data) {
      ret.add(tree.getData().getPath());
    }
    return ret;
  }
  
  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#removeAllData()
   */
  @Override
  public synchronized void removeAllData() {
    this.data.clear();
  }

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#getRenderingData()
   */
  @Override
  public synchronized DataTreeView[] getRenderingData() {
    return data.toArray(new DataTreeView[0]);
  }

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#getNumberRows()
   */
  @Override
  public synchronized int getNumberRows() {
    return numberRows ;
  }

  /* (non-Javadoc)
   * @see prosevis.processing.ProseModelIF#setNumberRows()
   */
  @Override
  public void setNumberRows(int numRows) {
    numberRows = numRows;
  }

}
