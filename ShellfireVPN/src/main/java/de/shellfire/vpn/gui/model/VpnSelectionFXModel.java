package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.webservice.Vpn;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class VpnSelectionFXModel {

	private SimpleStringProperty id;
	private ObjectProperty<Vpn> account_art;

	// vpn does not need property Object because it's just a storage of the vpn in question
	private Vpn vpn;

	/**
	 * Default constructor.
	 */
	public VpnSelectionFXModel() {
		this(0, null);
	}

	/**
	 * Constructor with some initial data.
	 *
	 * @param vpn_id
	 * @param vpn_type
	 * @param vpn_account_art
	 */
	public VpnSelectionFXModel(int vpn_id, Vpn vpn_account_art) {
		this.id = new SimpleStringProperty("sf" + vpn_id);
		this.account_art = new SimpleObjectProperty<Vpn>(vpn_account_art);

	}

	public String getId() {
		return id.get();
	}

	public void setId(String id) {
		this.id.set(id);
	}

	public SimpleStringProperty idProperty() {
		return id;
	}

	public Vpn getAccount_art() {
		return account_art.get();
	}

	public void setAccount_art(Vpn account_art) {
		this.account_art.set(account_art);
	}

	public ObjectProperty<Vpn> accountArtProperty() {
		return account_art;
	}

	public Vpn getVpn() {
		return vpn;
	}

	public void setVpn(Vpn vpn) {
		this.vpn = vpn;
	}

}
