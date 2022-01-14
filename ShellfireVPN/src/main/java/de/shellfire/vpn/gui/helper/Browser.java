package de.shellfire.vpn.gui.helper;

import java.util.Set;

import de.shellfire.vpn.gui.controller.RegisterFormController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSException;

public class Browser extends Region {

	final WebView webview;
    final WebEngine webEngine;
	private VBox vboxRegisterForm;

	private Point2D pLimit;
	private double width, height;
	private RegisterFormController controller;

	
    public Browser(WebView webview, String content, VBox vboxRegisterForm, RegisterFormController controller) {
    	this.webview = webview;
    	this.webEngine = webview.getEngine();
    	this.vboxRegisterForm = vboxRegisterForm;
    	this.controller = controller;
    	
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
        
        
        // disable context menu (copy option)
        webview.setContextMenuEnabled(false);

        WebEventDispatcher webEventDispatcher = new WebEventDispatcher(webview.getEventDispatcher());
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

            @Override
            public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
                if(newValue.equals(State.SUCCEEDED)){
                    // dispatch all events
                	webview.setEventDispatcher(webEventDispatcher);
                }
            }

        });
        
        setContent( content );
        
        webview.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {

            @Override
            public void onChanged(Change<? extends Node> c) {
                pLimit=webview.localToScene(webview.getWidth(),webview.getHeight());
                webview.lookupAll(".scroll-bar").stream().map(s->(ScrollBar)s).forEach(s->{
                            if(s.getOrientation().equals(Orientation.VERTICAL)){
                                width=s.getBoundsInLocal().getWidth();
                            }
                            if(s.getOrientation().equals(Orientation.HORIZONTAL)){
                                height=s.getBoundsInLocal().getHeight();
                            }
                        });
                // dispatch all events
                webEventDispatcher.setLimit(pLimit.subtract(width, height));
            }
        });
        
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
    					Double height = new Double(i);
    					height = height + 10;
    					webview.setPrefHeight(height);
    					
    					Double windowHeight = height + 276;
    					vboxRegisterForm.setPrefHeight(windowHeight);
    					controller.getStage().sizeToScene();
    					

    					
    				}
    			} catch (JSException e) {
    				// not important
    			} 
    		}               
    	});

	}
	
	private String getHtml(String content) {
		return "<html><style type=\"text/css\">a { cursor:hand;  } </style><body style=\"margin-top:0;padding-top:0;cursor:default;background-color: rgb(240,240,240);color:#323232;font-family:System; font-size:12px\">" +
				"<div id=\"mydiv\">" + content + "</div>" +
				"</body></html>";
	}

}