package utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;

class null implements ActionListener {
  private final GraphLayers this$0;
  
  null(GraphLayers this$0) {
    this.this$0 = this$0;
  }
  
  public void actionPerformed(ActionEvent e) {
    JCheckBox ch = (JCheckBox)e.getSource();
    this.this$0.setVisible(ch.getText(), !this.this$0.isVisible(ch.getText()));
  }
}
