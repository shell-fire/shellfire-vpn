/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.i18n.VpnI18N;
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
	private final ObjectProperty<VpnStar> speed;
	private final ObjectProperty<Server> country;


	public ServerListFXModel(Server server) {
		this.country = new SimpleObjectProperty(server);
		this.name = new SimpleStringProperty("<span class=\"city\">"+server.getCity()+"</span> <span class=\"country\">"+VpnI18N.getCountryI18n().getCountryName(server.getCountry())+"</span><br /><span class=\"servername\">"+server.getName()+"</span>");
		this.speed = new SimpleObjectProperty(server.getServerSpeed());
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
