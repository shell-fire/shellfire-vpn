/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Tcheutchoua Steve
 */
public class ServerRow {
	private final StringProperty name;
	private final ObjectProperty<ServerType> serverType;
	private final ObjectProperty<Server> server;
	private BooleanProperty selected;

	public ServerRow(Server server) {
		this.server = new SimpleObjectProperty<Server>(server);
		this.name = new SimpleStringProperty("<span class=\"city\">"+server.getCity()+"</span> <span class=\"country\">"+VpnI18N.getCountryI18n().getCountryName(server.getCountry())+"</span><br /><span class=\"servername\">"+server.getName()+"</span>");
		this.serverType = new SimpleObjectProperty<ServerType>(server.getServerType());
		this.selected = new SimpleBooleanProperty(false);
	}

	public Server getServer() {
		return server.get();
	}

	public void setCountry(Server country) {
		this.server.set(country);
	}

	public ObjectProperty<Server> countryProperty() {
		return server;
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

	public ServerType getServerType() {
		return serverType.get();
	}

	public void setSpeed(ServerType serverType) {
		this.serverType.set(serverType);
	}

	public ObjectProperty<ServerType> serverTypeProperty() {
		return serverType;
	}


	@Override
	public boolean equals(Object server) {
		if (server == null) {
			return false;
		} else if (server instanceof Server) {
			Server srv = (Server)server;
			if (this.server.get() != null) {
				return this.getServer().getServerId() == srv.getServerId();
			} else {
				return false;
			}
		} else if (server instanceof ServerRow) {
			ServerRow srvRow = (ServerRow)server;
			if (this.server.get() != null) {
				return this.getServer().getServerId() == srvRow.getServer().getServerId();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
