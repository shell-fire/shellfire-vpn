/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import java.util.LinkedList;

import de.shellfire.vpn.webservice.model.VpnAttributeContainer;
import de.shellfire.vpn.webservice.model.VpnAttributeElement;

/**
 *
 * @author bettmenn
 */
public class AttributeContainer {

  private final VpnAttributeHeader containerName;
  private LinkedList<AttributeElement> elements = new LinkedList<AttributeElement>();

  AttributeContainer(String containerName) {
    this.containerName = new VpnAttributeHeader(containerName);
  }

  public AttributeContainer(VpnAttributeContainer vpnAttributeContainer) {
    this.containerName = new VpnAttributeHeader(vpnAttributeContainer.getContainerName());

    VpnAttributeElement[] elmnts = vpnAttributeContainer.getElements();

    for (int i = 0; i < elmnts.length; i++) {
      VpnAttributeElement elmnt = elmnts[i];
      this.elements.add(new AttributeElement(elmnt));

    }
  }

  public VpnAttributeHeader getContainerName() {
    return containerName;
  }

  public LinkedList<AttributeElement> getElements() {
    return elements;
  }

}
