/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.renderer;

import java.util.HashMap;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.controller.VpnSelectDialogController;
import de.shellfire.vpn.gui.model.VpnSelectionFXModel;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.Vpn;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Tcheutchoua Steve
 */
public class CrownImageRendererVpn extends TableCell<VpnSelectionFXModel, Vpn> {

	static HashMap<ServerType, Image> icons = new HashMap<ServerType, Image>();
	static 	HashMap<ServerType, Image> iconsSelected = new HashMap<ServerType, Image>();
	static HashMap<ServerType, Image> iconsDisabled = new HashMap<ServerType, Image>();
	private VpnSelectDialogController vpnSelectDialogController;
	private static final Logger log = Util.getLogger(CrownImageRendererVpn.class.getCanonicalName());

	public CrownImageRendererVpn(VpnSelectDialogController vpnSelectDialogController) {
		this.vpnSelectDialogController = vpnSelectDialogController;

	}

	static {
		icons.put(ServerType.Free, Util.getImageIconFX("/images/crowns_1.png"));
		icons.put(ServerType.Premium, Util.getImageIconFX("/images/crowns_2.png"));
		icons.put(ServerType.PremiumPlus, Util.getImageIconFX("/images/crowns_3.png"));

		iconsSelected.put(ServerType.Free, Util.getImageIconFX("/images/crowns_1_selected.png"));
		iconsSelected.put(ServerType.Premium, Util.getImageIconFX("/images/crowns_2_selected.png"));
		iconsSelected.put(ServerType.PremiumPlus, Util.getImageIconFX("/images/crowns_3_selected.png"));
	}

    @Override
    protected void updateItem(Vpn item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setGraphic(null);
        } else {
        	boolean isSelected = (vpnSelectDialogController.getSelectedVpn() != null && vpnSelectDialogController.getSelectedVpn().equals(item));
        	updateItemSelected(item, isSelected);
        }
    }
    
	protected void updateItemSelected(Vpn item, boolean isSelected) {
		Image img = this.getIcon(item.getAccountType(), isSelected);
		ImageView imageView = new ImageView(img);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(20);
		setGraphic(imageView);
		getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

	}

	public Image getIcon(ServerType type, boolean isSelected) {
		return getIcon(type, isSelected, isDisabled());
	}
	
	public static Image getIcon(ServerType type, boolean isSelected, boolean isDisabled) {
		
		if (isSelected) {
			return iconsSelected.get(type);
		} // else if (!isDisabled())
		else if (isDisabled) {
			return iconsSelected.get(type);
		} else {
			return icons.get(type);
		}
	}
}