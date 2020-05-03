/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.types;

import java.awt.image.BufferedImage;

import de.shellfire.vpn.client.Controller;

/**
 *
 * @author bettmenn
 */
public interface LocatableIcon {
    public BufferedImage getIcon();

    public String getCountryString();
    
    public Controller getController();
    public void setController(Controller controller);
}
