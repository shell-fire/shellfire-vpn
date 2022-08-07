module ShellfireVPN {
	exports de.shellfire.vpn.webservice.model;
	exports de.shellfire.vpn.service;
	exports de.shellfire.vpn.exception;
	exports de.shellfire.vpn;
	exports de.shellfire.vpn.client;
	exports de.shellfire.vpn.gui.helper;
	exports de.shellfire.vpn.proxy;
	exports de.shellfire.vpn.service.win;
	exports de.shellfire.vpn.gui.model;
	exports de.shellfire.vpn.gui;
	exports de.shellfire.vpn.gui.controller;
	exports de.shellfire.vpn.gui.renderer;
	exports de.shellfire.vpn.messaging;
	exports de.shellfire.vpn.types;
	exports de.shellfire.vpn.i18n;
	exports de.shellfire.vpn.updater;
	exports de.shellfire.vpn.webservice;
	exports de.shellfire.vpn.client.win;

	requires LibFX;

	requires com.google.gson;
	requires commons.validator;
	requires gettext.commons;
	requires java.desktop;
	requires java.logging;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.swing;
	requires javafx.web;
	requires jdk.jsobject;

	requires logback.classic;
	requires logback.core;
	requires org.apache.commons.codec;
	requires org.apache.commons.io;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
	requires org.hyperic.sigar;
	// requires org.jfxtras.styles.jmetro;
	requires org.mozilla.javascript;
	requires registry;
	requires org.slf4j;
	requires chronicle.queue;
	requires chronicle.bytes;
	requires chronicle.wire;
	requires jsr305;
	
	
	opens de.shellfire.vpn.gui.controller to javafx.fxml;
	opens de.shellfire.vpn.webservice to com.google.gson;
	opens de.shellfire.vpn.webservice.model to com.google.gson; 
	opens de.shellfire.vpn.gui.helper to javafx.fxml;
	opens de.shellfire.vpn.updater to javafx.swing;
}
