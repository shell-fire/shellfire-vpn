package de.shellfire.vpn.gui;

public class VpnTrayMessage {

	private final String caption;
	private final String text;

	public VpnTrayMessage(String caption, String text) {

		this.caption = caption;
		this.text = text;

	}

	public String getCaption() {
		return this.caption;
	}

	public String getText() {
		return this.text;
	}

}
