/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.io.IOException;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

/**
 *
 * @author Tcheutchoua Steve
 */
public class FxUIManager {

	private static final Logger log = Util.getLogger(FxUIManager.class.getCanonicalName());

	public static Pane SwitchSubview(Object controller, String view) throws IOException {
		log.debug("SwitchSubview with two object and view parameters has controller " + controller.toString());
		FXMLLoader loader = new FXMLLoader(FxUIManager.class.getResource("/fxml/" + view));
		loader.setController(controller);

		return (AnchorPane) loader.load();
	}

	public static Pair<Pane, Object> SwitchSubview(String view) throws IOException {
		FXMLLoader loader = new FXMLLoader(FxUIManager.class.getResource("/fxml/" + view));

		Pane anchorPane = (AnchorPane) loader.load();
		log.debug("SwitchSubview loading the controller to " + view);
		Object controller = loader.getController();
		return new Pair<>(anchorPane, controller);
	}
}