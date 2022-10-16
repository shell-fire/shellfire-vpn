package de.shellfire.vpn.gui.helper;

import java.io.IOException;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.controller.AppScreenControllerAbout;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class AboutItem extends VBox {
	
	private static final Logger log = Util.getLogger(HelpItem.class.getCanonicalName());
	@FXML
	private ImageView helpArrowImage;
	@FXML
	private Label header;
	@FXML
	private Label text;
	private AppScreenControllerAbout appScreenControllerAbout;
	private WebViewHyperlinkListener eventPrintingListener;

	
	public AboutItem(AppScreenControllerAbout appScreenControllerAbout, String header, String text) {
		this.appScreenControllerAbout = appScreenControllerAbout;
        FXMLLoader loader = new FXMLLoader(HelpItem.class.getClassLoader().getResource("/fxml/about_item.fxml")); 
        loader.setLocation(HelpItem.class.getResource("/fxml/about_item.fxml"));
        		
        loader.setRoot(this);
        loader.setController(this);

        try {
        	loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
		this.header.setText(header);
		this.text.setText(text);
	}
	

}
