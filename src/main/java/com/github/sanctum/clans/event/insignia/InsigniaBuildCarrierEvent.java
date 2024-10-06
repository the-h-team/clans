package com.github.sanctum.clans.event.insignia;

import com.github.sanctum.clans.model.LogoHolder;
import com.github.sanctum.clans.event.ClanEvent;

/**
 * Called when a clan insignia is attempting to be built.
 */
public class InsigniaBuildCarrierEvent extends ClanEvent {

	private final LogoHolder.Carrier carrier;
	private final int line;
	private final int totalSize;
	private String content;

	public InsigniaBuildCarrierEvent(LogoHolder.Carrier carrier, int line, int totalSize, String content) {
		super(false);
		this.line = line;
		this.totalSize = totalSize;
		this.content = content;
		this.carrier = carrier;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public int getLine() {
		return line;
	}

	public int getTotalLines() {
		return totalSize;
	}

	public LogoHolder.Carrier getCarrier() {
		return carrier;
	}
}
