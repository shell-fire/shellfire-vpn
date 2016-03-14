package de.shellfire.vpn.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import de.shellfire.vpn.gui.model.ContentPane;

public class TableHeaderRenderer extends JLabel implements TableCellRenderer{

	  private Color fgColor = Color.white;
	  private Color bgColor = ContentPane.colorDarkGrey;

	 public TableHeaderRenderer() {
	    setOpaque(true);
	 }

	     public void setForegroundColor(Color fgColor) {
	       this.fgColor=fgColor;
	     }
	     public Color getForegroundColor() {
	       return fgColor;
	     }
	     public void setBackgroundColor(Color bgColor) {
	       this.bgColor=bgColor;
	     }
	     public Color getBackgroundColor() {
	       return bgColor;
	     }

	     public Component getTableCellRendererComponent(JTable table, Object value,
	                        boolean isSelected, boolean hasFocus, int row, int column) {
	               JLabel label = this;
	               label.setText((value ==null) ? "" : (" "+value.toString()));
	               label.setForeground(getForegroundColor());
	              label.setBackground(getBackgroundColor());
	                  return label;
	       }
	}