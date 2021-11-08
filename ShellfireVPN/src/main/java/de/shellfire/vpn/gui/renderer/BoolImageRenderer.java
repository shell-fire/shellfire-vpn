/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.shellfire.vpn.gui.renderer;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.model.VpnComparisonFXTableModel;
import de.shellfire.vpn.webservice.model.VpnEntry;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author TcheutchouaSteve on May 28, 2018
 */
public class BoolImageRenderer extends TableCell<VpnComparisonFXTableModel, VpnEntry> {
	Image iconTrue = Util.getImageIconFX("/icons/yes.png");
	Image iconFalse = Util.getImageIconFX("/icons/no.png");

	@Override
	public void updateItem(VpnEntry item, boolean empty) {
		super.updateItem(item, empty);
		Boolean b = item.isBool();
		Image icon = this.getIcon(b);

		setGraphic(new ImageView(icon));
		setTextAlignment(TextAlignment.CENTER);

	}

	public Image getIcon(Boolean bool) {
		if (bool) {
			return this.iconTrue;
		} else {
			return this.iconFalse;
		}
	}
}
