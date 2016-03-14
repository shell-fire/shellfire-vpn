/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;


import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.Vpn;

/**
 *
 * @author bettmenn
 */
public class VpnSelectionTableModel extends AbstractTableModel {
    private static I18n i18n = VpnI18N.getI18n();
    private String[] header = {i18n.tr("Id"), i18n.tr("Typ"), i18n.tr("Account Art")};
    
    
    
    private LinkedList<Vpn> vpns;


    public VpnSelectionTableModel(LinkedList<Vpn> allVpn) {
        this.vpns = allVpn;
    }
    
    public int getRowCount() {
        return vpns.size();
    }

    public int getColumnCount() {
        return header.length;
    }

    public String getColumnName(int columnIndex) {
        return this.header[columnIndex];
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vpn vpn = this.vpns.get(rowIndex);
        
        
        switch (columnIndex) {
            case 0:
                return "sf" + vpn.getVpnId();
            case 1:
                return vpn.getProductType();
            case 2: 
                return vpn.getAccountType();
            default:
                return 0;
        }
        
    }

    public Vpn getVpn(int selected) {
        return this.vpns.get(selected);
    }

    
}
