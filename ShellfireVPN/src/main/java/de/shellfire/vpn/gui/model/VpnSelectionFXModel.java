package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.webservice.Vpn;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VpnSelectionFXModel {

  private IntegerProperty id;
  private StringProperty type;
  private StringProperty account_art;

  // vpn does not need property Object because it's just a storage of the vpn in question
  private Vpn vpn;

  /**
   * Default constructor.
   */
  public VpnSelectionFXModel() {
    this(0, null, null);
  }

  /**
   * Constructor with some initial data.
   *
   * @param vpn_id
   * @param vpn_type
   * @param vpn_account_art
   */
  public VpnSelectionFXModel(int vpn_id, String vpn_type, String vpn_account_art) {
    this.id = new SimpleIntegerProperty(vpn_id);
    this.type = new SimpleStringProperty(vpn_type);
    this.account_art = new SimpleStringProperty(vpn_account_art);

  }

  public int getId() {
    return id.get();
  }

  public void setId(int id) {
    this.id.set(id);
  }

  public IntegerProperty idProperty() {
    return id;
  }

  public String getType() {
    return type.get();
  }

  public void setType(String type) {
    this.type.set(type);
    ;
  }

  public StringProperty typeProperty() {
    return type;
  }

  public String getAccount_art() {
    return account_art.get();
  }

  public void setAccount_art(String account_art) {
    this.account_art.set(account_art);
  }

  public StringProperty accountArtProperty() {
    return account_art;
  }

  public Vpn getVpn() {
    return vpn;
  }

  public void setVpn(Vpn vpn) {
    this.vpn = vpn;
  }

}
