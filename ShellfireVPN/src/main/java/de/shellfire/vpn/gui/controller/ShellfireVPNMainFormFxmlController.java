package de.shellfire.vpn.gui.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.ShellfireVPNMainForm;
import de.shellfire.vpn.i18n.VpnI18N;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;

public class ShellfireVPNMainFormFxmlController extends AnchorPane implements Initializable {

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
	
	
	private final static HashMap<String, Image> mainIconMap = new HashMap<String, Image>() {
		{
			put("de", Util.getImageIconFX("src/main/resources/icons/sf.png"));
			put("en", Util.getImageIconFX("src/main/resources/icons/sf_en.png"));
			put("fr", Util.getImageIconFX("src/main/resources/icons/sf_fr.png"));
		}
	};

	public static Image getLogo() {
		  Image imagelogo = ShellfireVPNMainFormFxmlController.mainIconMap.get(VpnI18N.getLanguage().getKey());
		  System.out.println("The image key is found at "+VpnI18N.getLanguage().getKey());
		  
			return imagelogo;
		}

}
