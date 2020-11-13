/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import java.util.LinkedList;

import de.shellfire.vpn.webservice.model.VpnAttributeContainer;
import de.shellfire.vpn.webservice.model.VpnAttributeList;

/**
 * 
 * @author bettmenn
 */
class AttributeList {

	LinkedList<AttributeContainer> containers = new LinkedList<AttributeContainer>();

	public AttributeList() {

	}

	public AttributeList(VpnAttributeList vpnComparisonTable) {
		VpnAttributeContainer[] cnts = vpnComparisonTable.getContainers();
		for (int i = 0; i < cnts.length; i++) {
			AttributeContainer cnt = new AttributeContainer(cnts[i]);
			containers.add(cnt);
		}

	}

	public void add(AttributeContainer attributeContainer) {
		this.containers.add(attributeContainer);
	}

	public LinkedList<AttributeContainer> getContainers() {
		return containers;
	}

	int getRowCount() {
		int result = 0;
		for (AttributeContainer container : this.containers) {
			result += 1 + container.getElements().size();
		}

		return result;

	}

	Object getValueAt(int rowIndex, int columnIndex) {
		int currentPos = 0;

		// loop to requested row
		for (AttributeContainer container : this.containers) {
			int delta = rowIndex - currentPos;

			if (delta < container.getElements().size() + 1) {
				// found the right container

				if (delta == 0) {
					// container name
					if (columnIndex == 0) {
						return container.getContainerName();
					} else {
						return "";
					}
				} else {
					AttributeElement line = container.getElements().get(delta - 1);

					if (columnIndex == 0) {
						return line.getName();
					} else if (columnIndex == 1) {
						return line.getFree();
					} else if (columnIndex == 2) {
						return line.getPremium();
					} else if (columnIndex == 3) {
						return line.getPremiumPlus();
					}
				}
			}

			currentPos += container.getElements().size() + 1;

		}

		return "";
	}

}
