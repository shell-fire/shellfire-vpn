/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;


import javax.swing.table.AbstractTableModel;

import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.ServerList;

/**
 *
 * @author bettmenn
 */
public class ServerListTableModel extends AbstractTableModel {
    private static I18n i18n = VpnI18N.getI18n();

    private String[] header = {i18n.tr("Country"), i18n.tr("Name"), i18n.tr("Server type"), i18n.tr("Security"), i18n.tr("Speed")};
    
    private ServerList serverList;

    public ServerListTableModel(ServerList serverList) {
        this.serverList = serverList;
    }
    
    public int getRowCount() {
      if (serverList == null) {
        return 10;
      } else {
        return serverList.getNumberOfServers();
      }
        
    }

    public int getColumnCount() {
        return header.length;
    }

    public String getColumnName(int columnIndex) {
        return this.header[columnIndex];
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
      if (this.serverList == null) {
        return null;
      }
      if (rowIndex > this.serverList.getNumberOfServers()) {
        return null;
      }
        
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
