/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.shellfire.vpn.VpnEntry;

/**
 *
 * @author bettmenn
 */
public class StarImageRenderer extends DefaultTableCellRenderer {

    JLabel lbl = new JLabel();
    HashMap<Integer, ImageIcon> icons = new HashMap <Integer, ImageIcon>(); 
    HashMap<Integer, ImageIcon> iconsSelected = new HashMap <Integer, ImageIcon>(); 
    HashMap<Integer, ImageIcon> iconsDisabled = new HashMap <Integer, ImageIcon>(); 
    
    
    private void init() {
      
      icons.put(1, new ImageIcon(getClass().getResource("1star.png")));
      icons.put(2, new ImageIcon(getClass().getResource("2star.png")));
      icons.put(3, new ImageIcon(getClass().getResource("3star.png")));
      icons.put(4, new ImageIcon(getClass().getResource("4star.png")));
      icons.put(5, new ImageIcon(getClass().getResource("5star.png")));      

      iconsSelected.put(1, new ImageIcon(getClass().getResource("1star_selected.png")));
      iconsSelected.put(2, new ImageIcon(getClass().getResource("2star_selected.png")));
      iconsSelected.put(3, new ImageIcon(getClass().getResource("3star_selected.png")));
      iconsSelected.put(4, new ImageIcon(getClass().getResource("4star_selected.png")));
      iconsSelected.put(5, new ImageIcon(getClass().getResource("5star_selected.png")));      

      iconsDisabled.put(1, new ImageIcon(getClass().getResource("1star_disabled.png")));
      iconsDisabled.put(2, new ImageIcon(getClass().getResource("2star_disabled.png")));
      iconsDisabled.put(3, new ImageIcon(getClass().getResource("3star_disabled.png")));
      iconsDisabled.put(4, new ImageIcon(getClass().getResource("4star_disabled.png")));
      iconsDisabled.put(5, new ImageIcon(getClass().getResource("5star_disabled.png")));      
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      init();
      setEnabled(table == null || table.isEnabled());
      lbl.setOpaque(true);
      lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11));
      
      if (isEnabled()) {
      	lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ContentPane.colorLightGray));
      } else {
      	lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ContentPane.colorVeryLightGray));
      }
      
      if (isSelected) {
		if (isEnabled()) {
			lbl.setForeground(ContentPane.colorDarkGrey);
		} else {
			lbl.setForeground(Color.darkGray);
		}
        
        lbl.setBackground(table.getSelectionBackground());
      } else {
			if (isEnabled()) {
				lbl.setBackground(table.getBackground());
				lbl.setForeground(table.getForeground());
			} else {
				lbl.setBackground(Color.white);
				lbl.setForeground(Color.lightGray);
			}     
      }
      
      VpnStar star; 
      if (value instanceof VpnStar) {
        star = (VpnStar)value;
      } else {
        VpnEntry e = (VpnEntry) value;
        star = new VpnStar(e.getStar());  
      }
      
      
        ImageIcon icon = this.getIcon(star, isSelected);
        lbl.setText(star.getText());
        lbl.setIcon(icon);
        lbl.setHorizontalAlignment(JLabel.LEFT);
        lbl.validate();
        
        return lbl;
    }

    public ImageIcon getIcon(VpnStar star, boolean isSelected) {
      if (isSelected)
        return iconsSelected.get(star.getNum());
      else if (isEnabled())
        return icons.get(star.getNum());
      else
        return iconsDisabled.get(star.getNum());
    }


}
