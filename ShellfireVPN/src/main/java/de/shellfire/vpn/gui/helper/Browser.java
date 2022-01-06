package de.shellfire.vpn.gui.helper;

import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;

public class Browser extends Region {

	final WebView webview;
    final WebEngine webEngine;
     
    public Browser(WebView webview, String content ) {
    	this.webview = webview;
    	this.webEngine = webview.getEngine();
    	
    	webview.setPrefHeight(5);
    	
    	this.setPadding(new Insets(20));
    	
        widthProperty().addListener( new ChangeListener<Object>() {
	    	public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) 
	    	{ 
	    		Double width = (Double)newValue;
	    		System.out.println("Region width changed: " + width);
	    		webview.setPrefWidth(width);
	    		adjustHeight();
	    	}    
	    });
        

        webview.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> arg0, State oldState, State newState) {
				if (newState == State.SUCCEEDED) {
					adjustHeight();
				}				
			}
		});
        
        
        // http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
        webview.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
        	@Override public void onChanged(Change<? extends Node> change) {
        		Set<Node> scrolls = webview.lookupAll(".scroll-bar");
        		for (Node scroll : scrolls) {
        			scroll.setVisible(false);
    			}
    		}
    	});
        
        setContent( content );
        
        // getChildren().add(webview);
    }
    
    public void setContent( final String content )
    {
    	Platform.runLater(new Runnable(){
    		@Override                                
    		public void run() {
    			webEngine.loadContent(getHtml(content));
    			Platform.runLater(new Runnable(){
    	    		@Override                                
    	    		public void run() {
    	    			adjustHeight();
    	    		}               
    	    	});
    		}               
    	});
    }
    
 
    @Override 
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webview,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }
    
	private void adjustHeight() {
		
		Platform.runLater(new Runnable(){
    		@Override                                
    		public void run() {
    			try {
    				//"document.getElementById('mydiv').offsetHeight"
    				Object result = webEngine.executeScript(
    						"document.getElementById('mydiv').offsetHeight");
    				if (result instanceof Integer) {
    					Integer i = (Integer) result;
    					double height = new Double(i);
    					height = height + 20;
    					webview.setPrefHeight(height);
    					System.out.println("height on state: " + height + " prefh: " + webview.getPrefHeight());
    				}
    			} catch (JSException e) {
    				// not important
    			} 
    		}               
    	});

	}
	
	private String getHtml(String content) {
		return "<html><body>" +
				"<div id=\"mydiv\">" + content + "</div>" +
				"</body></html>";
	}

}