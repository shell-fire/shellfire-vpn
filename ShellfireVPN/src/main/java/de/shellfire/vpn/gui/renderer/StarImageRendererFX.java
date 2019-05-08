/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.renderer;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.model.ServerListFXModel;
import de.shellfire.vpn.webservice.model.VpnStar;
import java.util.HashMap;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;

/**
 *
 * @author Tcheutchoua Steve
 */
public class StarImageRendererFX extends TableCell<ServerListFXModel, VpnStar> {

    HashMap<Integer, Image> icons = new HashMap<Integer, Image>();
    HashMap<Integer, Image> iconsSelected = new HashMap<Integer, Image>();
    HashMap<Integer, Image> iconsDisabled = new HashMap<Integer, Image>();
    private static final Logger log = Util.getLogger(StarImageRendererFX.class.getCanonicalName());

    public StarImageRendererFX() {
        init();
    }

    private void init() {
        String baseUrl = "src/main/resources";
        icons.put(1, Util.getImageIconFX(baseUrl + "/icons/stars/1star.png"));
        icons.put(2, Util.getImageIconFX(baseUrl + "/icons/stars/2star.png"));
        icons.put(3, Util.getImageIconFX(baseUrl + "/icons/stars/3star.png"));
        icons.put(4, Util.getImageIconFX(baseUrl + "/icons/stars/4star.png"));
        icons.put(5, Util.getImageIconFX(baseUrl + "/icons/stars/5star.png"));

        iconsSelected.put(1, Util.getImageIconFX(baseUrl + "/icons/stars/1star_selected.png"));
        iconsSelected.put(2, Util.getImageIconFX(baseUrl + "/icons/stars/2star_selected.png"));
        iconsSelected.put(3, Util.getImageIconFX(baseUrl + "/icons/stars/3star_selected.png"));
        iconsSelected.put(4, Util.getImageIconFX(baseUrl + "/icons/stars/4star_selected.png"));
        iconsSelected.put(5, Util.getImageIconFX(baseUrl + "/icons/stars/5star_selected.png"));

        iconsDisabled.put(1, Util.getImageIconFX(baseUrl + "/icons/stars/1star_disabled.png"));
        iconsDisabled.put(2, Util.getImageIconFX(baseUrl + "/icons/stars/2star_disabled.png"));
        iconsDisabled.put(3, Util.getImageIconFX(baseUrl + "/icons/stars/3star_disabled.png"));
        iconsDisabled.put(4, Util.getImageIconFX(baseUrl + "/icons/stars/4star_disabled.png"));
        iconsDisabled.put(5, Util.getImageIconFX(baseUrl + "/icons/stars/5star_disabled.png"));
    }

    @Override
    protected void updateItem(VpnStar item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
            log.debug("StarImageRendererFX: Star Image and text could not be rendered");
            setText("Empty");
        } else {
            Image img = this.getIcon(item, isSelected());
            setGraphic(new ImageView(img));
            getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            log.debug("StarImageRendererFX: " + item.getNum());
            setText(item.getText());
        }
        this.setDisable(true);
        this.setDisabled(true);
    }

    public Image getIcon(VpnStar star, boolean isSelected) {
        if (isSelected) {
            return iconsSelected.get(star.getNum());
        } //else if (!isDisabled())
        else if (isDisabled() == false) {
            return icons.get(star.getNum());
        } else {
            return iconsDisabled.get(star.getNum());
        }
    }
}
