package de.shellfire.vpn.gui.helper;

import java.io.IOException;

import javax.swing.event.HyperlinkEvent;

import org.codefx.libfx.control.webview.WebViewHyperlinkListener;
import org.codefx.libfx.control.webview.WebViews;
import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.controller.AppScreenControllerHelp;
import de.shellfire.vpn.gui.controller.ShellfireVPNMainFormFxmlController;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

public class HelpItem extends Pane {
	
	private static final Logger log = Util.getLogger(HelpItem.class.getCanonicalName());
	private static Image helpArrowImageHidden = Util.getImageIconFX("/icons/arrow_down.png");
	private static Image helpArrowImageVisible = Util.getImageIconFX("/icons/arrow_up.png");
	
	@FXML
	private ImageView helpArrowImage;
	@FXML
	private Label header;
	@FXML
	private WebView text;
	private AppScreenControllerHelp appScreenControllerHelp;
	private WebViewHyperlinkListener eventPrintingListener;

	
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
		this.text.getEngine().loadContent(getHtml(text));
		eventPrintingListener = event -> {

			// Check if the link has been clicked then, open in external browser.
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				 Util.openUrl(event.getURL());
			}

			return true;
		};
		WebViews.addHyperlinkListener(this.text, eventPrintingListener);
		
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

    private String getHtml(String content) {
    	content = content.replaceAll("(?i)href=\"/", "href=\"https://www.shellfire.de/");
    	content = content.replaceAll("(?i)href='/", "href='https://www.shellfire.de/");
    	
        return "<html>" +
                "<head>" +
                "<style>\n" +
                "body {" +
                "    font-family: Arial;\n" +
                "    font-size: 12px;\n" +
                "    font-weight: Regular;" +
                "    background-color: #fafafa;" +
                "    color: #666666;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id=\"mydiv\">" + content + "</div>" +
                "</body></html>";
    }
}
