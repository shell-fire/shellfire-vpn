/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;


import javax.swing.table.AbstractTableModel;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Server;
import de.shellfire.vpn.ServerList;

/**
 *
 * @author bettmenn
 */
public class ServerListTableModel extends AbstractTableModel {
    private static I18n i18n = VpnI18N.getI18n();

    private String[] header = {i18n.tr("Land"), i18n.tr("Name"), i18n.tr("Servertyp"), i18n.tr("Sicherheit"), i18n.tr("Geschwindigkeit")};
    
    private ServerList serverList;

    ServerListTableModel(ServerList serverList) {
        this.serverList = serverList;
    }
    
    public int getRowCount() {
        return serverList.getNumberOfServers();
    }

    public int getColumnCount() {
        return header.length;
    }

    public String getColumnName(int columnIndex) {
        return this.header[columnIndex];
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Server server = this.serverList.getServer(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return server.getCountry();
            case 1: 
                return server.getName();
            case 2: 
                return server.getServerType();
            case 3: 
              return server.getSecurity();
            case 4: 
                return server.getServerSpeed();
            default:
                return 0;
        }
        
    }

    public int getRowForServer(Server server) {
        return this.serverList.getServerNumberByServer(server);
    }
    
}
