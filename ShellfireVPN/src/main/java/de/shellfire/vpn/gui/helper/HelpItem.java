package de.shellfire.vpn.gui.helper;

import java.io.IOException;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.controller.AppScreenControllerHelp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class HelpItem extends Pane {
	
	private static Image helpArrowImageHidden = Util.getImageIconFX("/icons/arrow_down.png");
	private static Image helpArrowImageVisible = Util.getImageIconFX("/icons/arrow_up.png");
	
	@FXML
	private ImageView helpArrowImage;
	@FXML
	private Label header;
	@FXML
	private Label text;
	private AppScreenControllerHelp appScreenControllerHelp;

	
	public HelpItem(AppScreenControllerHelp appScreenControllerHelp, String header, String text) {
		this.appScreenControllerHelp = appScreenControllerHelp;
        FXMLLoader loader = new FXMLLoader(HelpItem.class.getClassLoader().getResource("/fxml/help_item.fxml")); 
        loader.setLocation(HelpItem.class.getResource("/fxml/help_item.fxml"));
        		
        loader.setRoot(this);
        loader.setController(this);

        try {
        	loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
		this.header.setText(header);
		this.text.setText(text);
		
		this.hide();
	}
	
	public void hide() {
        this.text.setVisible(false);
        this.text.setManaged(false);
        this.helpArrowImage.setImage(helpArrowImageHidden);
	}
	
	public void show() {
        this.text.setVisible(true);
        this.text.setManaged(true);
        this.helpArrowImage.setImage(helpArrowImageVisible);
	}

	@FXML
	private void onClickHeader(MouseEvent event) {
		boolean isVisible = this.text.isVisible();
		
		this.appScreenControllerHelp.hideAllHelpItems();
		
		if (!isVisible) {
			this.show();
		}
	}
	
}
