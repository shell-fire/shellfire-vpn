/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.renderer;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.shellfire.vpn.webservice.model.VpnEntry;

/**
 *
 * @author bettmenn
 */
public class BoolImageRenderer extends DefaultTableCellRenderer {

    JLabel lbl = new JLabel();
    ImageIcon iconTrue = new ImageIcon(getClass().getResource("/icons/yes.png"));
    ImageIcon iconFalse = new ImageIcon(getClass().getResource("/icons/no.png"));
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      VpnEntry e = (VpnEntry) value;
        Boolean b = e.isBool();
        
        ImageIcon icon = this.getIcon(b);
        
        
        //lbl.setText(text);
        lbl.setIcon(icon);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        return lbl;
    }

    public ImageIcon getIcon(Boolean bool) {
        if (bool) {
            return this.iconTrue;
        } else {
            return this.iconFalse;
        }

    }


}
