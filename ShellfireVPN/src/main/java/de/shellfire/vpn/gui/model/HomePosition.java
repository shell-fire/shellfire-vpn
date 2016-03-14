/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.OwnPositionPanel;
import de.shellfire.vpn.types.LocatableIcon;
import de.shellfire.vpn.webservice.model.WsGeoPosition;

/**
 *
 * @author bettmenn
 */
public class HomePosition implements LocatableIcon {

    private WsGeoPosition pos;
    private Image icon;
	private OwnPositionPanel panel;

    public HomePosition(WsGeoPosition pos) {
        this.pos = pos;
        this.icon = new ImageIcon(getClass().getResource("/de/shellfire/vpn/gui/resources/icon-home.png")).getImage();
    }

    @Override
    public GeoPosition getGeoPosition() {
        return new GeoPosition(this.pos.getLatitude(), this.pos.getLongitude());
    }

    @Override
    public Image getIcon() {
        return this.icon;
    }

    @Override
    public JPanel getPanel() {
    	if (this.panel == null)
    		this.panel = new OwnPositionPanel(this);
    	
    	
    	return this.panel;
    }


    @Override
    public String getCity() {
        return this.pos.getCity();
    }

    @Override
    public String getCountryString() {
        return this.pos.getCountry();
    }

	@Override
	public Controller getController() {
		return null;
	}

	@Override
	public void setController(Controller controller) {
	}
}
