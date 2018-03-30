/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author Tcheutchoua Steve
 */
public class FxUIManager {
    
    public static Pane SwitchSubview(Object controller, String view) throws IOException{
        FXMLLoader loader = new FXMLLoader(FxUIManager.class.getResource("/fxml/" + view));
        if (null != controller){
            loader.setController(controller);
        }
        return (AnchorPane)loader.load();
    }
}
