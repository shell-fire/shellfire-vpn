/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.ServerList;
import javafx.beans.property.StringProperty;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author Tcheutchoua Steve
 */
public class ServerListFXModel {
    
    private StringProperty land ;
    private StringProperty Name ;
    private StringProperty serverType ;
    private StringProperty security ;
    private StringProperty speed ;

    private static I18n i18n = VpnI18N.getI18n();
    private String[] header = {i18n.tr("Land"), i18n.tr("Name"), i18n.tr("Servertyp"), i18n.tr("Sicherheit"), i18n.tr("Geschwindigkeit")};
    private ServerList serverList;
    
    public String getLand() {
        return land.get();
    }

    public void setLand(String land) {
        this.land.set(land);
    }

    public String getName() {
        return Name.get();
    }

    public void setName(String name) {
        this.Name.set(name);
    }

    public String getServerType() {
        return serverType.get();
    }

    public void setServerType(String serverType) {
        this.serverType.set(serverType);
    }

    public String getSecurity() {
        return security.get();
    }

    public void setSecurity(String security) {
        this.security.set(security);
    }

    public String getSpeed() {
        return speed.get();
    }

    public void setSpeed(String speed) {
        this.speed.set(speed);
    }
    
    
}
