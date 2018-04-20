/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.model.VpnStar;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tcheutchoua Steve
 */
public class ServerListFXModel {
    private final StringProperty name;
    private final StringProperty serverType;
    private final ObjectProperty<VpnStar> security;
    private final ObjectProperty<VpnStar> speed;
    private final ObjectProperty<Server> country;

    
    //private static I18n i18n = VpnI18N.getI18n();

    //private String[] header = {i18n.tr("Land"), i18n.tr("Name"), i18n.tr("Servertyp"), i18n.tr("Sicherheit"), i18n.tr("Geschwindigkeit")};
    ///private ServerList serverList;
    public ServerListFXModel() {
        this(null, null, null, null, null);
    }

    public ServerListFXModel(Object country, String name, String serverType, Object security, Object speed) {
        this.country = new SimpleObjectProperty(country);
        this.name = new SimpleStringProperty(name);
        this.serverType = new SimpleStringProperty(serverType);
        this.security = new SimpleObjectProperty(security);
        this.speed = new SimpleObjectProperty(speed);
    }

    public Server getCountry() {
        return country.get();
    }

    public void setCountry(Server country) {
        this.country.set(country);
    }

    public ObjectProperty<Server> countryProperty() {
        return country;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public VpnStar getSecurity() {
        return security.get();
    }

    public void setSecurity(VpnStar security) {
        this.security.set(security);
    }

    public ObjectProperty<VpnStar> securityProperty() {
        return security;
    }

    public String getServerType() {
        return serverType.get();
    }

    public void setServerType(String serverType) {
        this.serverType.set(serverType);
    }

    public StringProperty serverTypeProperty() {
        return serverType;
    }

    public VpnStar getSpeed() {
        return speed.get();
    }

    public void setSpeed(VpnStar speed) {
        this.speed.set(speed);
    }

    public ObjectProperty<VpnStar> speedProperty() {
        return speed;
    }
    
}
