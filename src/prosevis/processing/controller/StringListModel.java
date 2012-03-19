package prosevis.processing.controller;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

class StringListModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private static final long serialVersionUID = -3773860122127724166L;
  private final ArrayList<String> labels = new ArrayList<String>();
  private int selectedIdx = -1;

  public void refresh(ArrayList<String> list) {
    this.labels.clear();
    this.labels.addAll(list);
    fireContentsChanged(this, 0, list.size() - 1);
  }

  @Override
  public String getElementAt(int index) {
    return labels.get(index);
  }

  @Override
  public int getSize() {
    return labels.size();
  }

  @Override
  public Object getSelectedItem() {
    return (selectedIdx == -1)?null:labels.get(selectedIdx);
  }

  @Override
  public void setSelectedItem(Object anItem) {
    if (!(anItem instanceof String)) {
      return;
    }
    String str = (String)anItem;
    for (int i = 0; i < labels.size(); i++) {
      if (str.equals(labels.get(i))) {
        selectedIdx = i;
        return;
      }
    }
  }

  public void addItem(String item) {
    if (!labels.contains(item)) {
      labels.add(item);
      fireContentsChanged(this, labels.size() - 1, labels.size() - 1);
    }
  }
}