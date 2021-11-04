package de.shellfire.vpn.gui.helper;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Adapted from this code: http://tech.chitgoks.com/2014/09/13/how-to-fit-webview-height-based-on-its-content-in-java-fx-2-2/
 */
public final class WebViewFitContent extends Region {

    /**
     * A logger for the WebViewFitContent class. Logs errors and messages of varying severity to a file or the console.
     * See logback.xml for details.
     */
    private static final Logger logger = LoggerFactory.getLogger(WebViewFitContent.class);

    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();

    public WebViewFitContent(String content) {
        webView.setPrefHeight(5);

        widthProperty().addListener((ChangeListener<Object>) (observable, oldValue, newValue) -> {
            Double width = (Double)newValue;
            webView.setPrefWidth(width);
            adjustHeight();
        });

        webView.getEngine().getLoadWorker().stateProperty().addListener((arg0, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                adjustHeight();
            }
        });

        webView.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
            Set<Node> scrolls = webView.lookupAll(".scroll-bar");
            for (Node scroll : scrolls) {
                scroll.setVisible(false);
            }
        });

        // We've been having a problem with WebViews that seem to be the right size (based on the fact
        // that their immediate parent acts like it's containing the webview) but the content of the WebView
        // does not show. As a workaround for this, anytime the user mouses over, we'll reload the content.
        this.setOnMouseEntered(event -> setContent(content));

        setContent(content);
        getChildren().add(webView);
    }

    public void setContent(final String content) {
        Platform.runLater(() -> {
            webEngine.loadContent(getHtml(content));
            Platform.runLater(this::adjustHeight);
        });
    }


    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webView,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    void adjustHeight() {
        Platform.runLater(() -> {
            try {
                Object result = webEngine.executeScript(
                        "var myDiv = document.getElementById('mydiv');" +
                        "if (myDiv != null) myDiv.offsetHeight");
                if (result instanceof Integer) {
                    Integer i = (Integer) result;
                    double height = new Double(i);
                    height = height + 20;
                    webView.setPrefHeight(height);
                }
            } catch (JSException e) {
                logger.warn("Something happened while adjusting the web view's height!", e);
            }
        });
    }

    private String getHtml(String content) {
        return "<html>" +
                "<head>" +
                "<style>\n" +
                ".mention {font-weight: bold;}" +
                "body {" +
                "    /*This is the same font that JavaFX uses*/\n" +
                "    font-family: System;\n" +
                "    font-size: 12px;\n" +
                "    font-weight: Regular;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id=\"mydiv\">" + content + "</div>" +
                "</body></html>";
    }

    public WebView getWebView() {
        return webView;
    }
}