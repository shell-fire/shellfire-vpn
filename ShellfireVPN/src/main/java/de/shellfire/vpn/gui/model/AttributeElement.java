/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import de.shellfire.vpn.webservice.model.VpnAttributeElement;
import de.shellfire.vpn.webservice.model.VpnEntry;

/**
 * 
 * @author bettmenn
 */
public class AttributeElement {

  private final String name;
  private final VpnEntry free;
  private final VpnEntry premium;
  private VpnEntry premiumPlus;

  public AttributeElement(VpnAttributeElement elmnt) {
    this.name = elmnt.getName();
    this.free = new VpnEntry(elmnt.getFree());
    this.premium = new VpnEntry(elmnt.getPremium());
    this.premiumPlus = new VpnEntry(elmnt.getPp());
  }

  public String getName() {
    return name;
  }

  public VpnEntry getFree() {
    return free;
  }

  public VpnEntry getPremium() {
    return premium;
  }

  public VpnEntry getPremiumPlus() {
    return premiumPlus;
  }
}
