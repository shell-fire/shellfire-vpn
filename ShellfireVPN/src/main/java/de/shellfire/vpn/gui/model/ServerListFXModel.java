/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import javafx.beans.property.StringProperty;

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
