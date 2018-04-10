package de.shellfire.vpn.types;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.xnap.commons.i18n.I18n;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.ServerInMapPanel;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.model.VpnStar;
import de.shellfire.vpn.webservice.model.WsServer;

public class Server implements LocatableIcon {

  private int serverId;
  private Country country;
  private String name;
  private String host;
  private ServerType serverType;
  private double longitude;
  private double latitude;
  private BufferedImage iconServerForMap;
  private Controller controller;
  private JPanel panel;
  private static I18n i18n = VpnI18N.getI18n();

  public Server(WsServer wss) {
    this.serverId = wss.getVpnServerId();

    String country = wss.getCountry();
    country = country.replace(" ", "");
    try {
      this.country = Enum.valueOf(Country.class, country);
    } catch (Exception e) {
      this.country = Country.Germany;
    }

    this.name = wss.getName();
    this.host = wss.getHost();
    this.serverType = Enum.valueOf(ServerType.class, wss.getServertype());
    this.longitude = wss.getLongitude();
    this.latitude = wss.getLatitude();
    
    try {
      this.iconServerForMap = ImageIO.read(getClass().getResourceAsStream("/icons/sf-server-map-32x32.png"));
    } catch (IOException e) {
      Util.handleException(e);
    }
  }

  public int getServerId() {
    return serverId;
  }

  public void setVpnServerId(int serverId) {
    this.serverId = serverId;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public ServerType getServerType() {
    return serverType;
  }

  public void setServerType(ServerType serverType) {
    this.serverType = serverType;
  }

  public VpnStar getServerSpeed() {
    switch (this.serverType) {
    case PremiumPlus:
      return new VpnStar(5, i18n.tr("unlimited kbit/sec"));
    case Premium:
      return new VpnStar(3, i18n.tr("up to 10,000 kbit/sec"));
    case Free:
    default:
      return new VpnStar(1, i18n.tr("up to 768 kbit/sec"));
    }
  }

  @Override
  public boolean equals(Object server) {
    if (server == null)
      return false;
    else if (!(server instanceof Server))
      return false;
    else {
      Server s = (Server) server;
      return s.getServerId() == this.getServerId();
    }

  }

  public double getLatitude() {
    return this.latitude;
  }

  public double getLongitude() {
    return this.longitude;
  }

  @Override
  public GeoPosition getGeoPosition() {
    return new GeoPosition(this.getLatitude(), this.getLongitude());
  }

  @Override
  public BufferedImage getIcon() {
    return this.iconServerForMap;
  }

  @Override
  public JPanel getPanel() {
    if (this.panel == null)
      this.panel = new ServerInMapPanel(this);

    return this.panel;
  }

  @Override
  public String getCity() {
    return null;
  }

  @Override
  public String getCountryString() {
    return this.getCountry().toString();
  }

  @Override
  public Controller getController() {
    return controller;
  }

  @Override
  public void setController(Controller controller) {
    this.controller = controller;

  }

  public VpnStar getSecurity() {
    switch (this.serverType) {
    case PremiumPlus:
      return new VpnStar(5, i18n.tr("256 bit"));
    case Premium:
      return new VpnStar(3, i18n.tr("192 bit"));
    case Free:
    default:
      return new VpnStar(2, i18n.tr("128 bit"));
    }
  }

  public String toString() {
    return "serverId= " + serverId + "\r\n" + "country= " + country + "\r\n" + "name= " + name + "\r\n" + "host= " + host + "\r\n"
        + "serrverType= " + serverType + "\r\n";
  }

}
