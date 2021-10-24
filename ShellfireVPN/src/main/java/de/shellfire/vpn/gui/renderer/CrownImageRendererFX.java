/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.renderer;

import java.util.HashMap;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.controller.ServerListSubviewController;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.types.ServerType;
import de.shellfire.vpn.webservice.WebService;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Tcheutchoua Steve
 */
public class CrownImageRendererFX extends TableCell<ServerListFXModel, Server> {

	HashMap<ServerType, Image> icons = new HashMap<ServerType, Image>();
	HashMap<ServerType, Image> iconsSelected = new HashMap<ServerType, Image>();
	HashMap<ServerType, Image> iconsDisabled = new HashMap<ServerType, Image>();
	private ServerListSubviewController serverListSubViewController;
	private static final Logger log = Util.getLogger(CrownImageRendererFX.class.getCanonicalName());

	public CrownImageRendererFX(ServerListSubviewController serverListSubviewController) {
		this.serverListSubViewController = serverListSubviewController;
		init();
	}

	private void init() {
		icons.put(ServerType.Free, Util.getImageIconFX("/images/crowns_1.png"));
		icons.put(ServerType.Premium, Util.getImageIconFX("/images/crowns_2.png"));
		icons.put(ServerType.PremiumPlus, Util.getImageIconFX("/images/crowns_3.png"));

		iconsSelected.put(ServerType.Free, Util.getImageIconFX("/images/crowns_1_selected.png"));
		iconsSelected.put(ServerType.Premium, Util.getImageIconFX("/images/crowns_2_selected.png"));
		iconsSelected.put(ServerType.PremiumPlus, Util.getImageIconFX("/images/crowns_3_selected.png"));
	}

    @Override
    protected void updateItem(Server item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty || item == null) {
            setGraphic(null);
        } else {
        	boolean isSelected = serverListSubViewController.getSelectedServer().equals(item);
        	updateItemSelected(item, isSelected);
        }
    }
    
	protected void updateItemSelected(Server item, boolean isSelected) {
		Image img = this.getIcon(item.getServerType(), isSelected);
		ImageView imageView = new ImageView(img);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(20);
		setGraphic(imageView);
		getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

		// this.setDisable(true);
		// this.setDisabled(true);
	}

	public Image getIcon(ServerType type, boolean isSelected) {
		
		if (isSelected) {
			return iconsSelected.get(type);
		} // else if (!isDisabled())
		else if (isDisabled()) {
			return iconsSelected.get(type);
		} else {
			return icons.get(type);
		}
	}
}