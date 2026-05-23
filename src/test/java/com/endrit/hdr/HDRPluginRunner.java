package com.endrit.hdr;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class HDRPluginRunner {
	private HDRPluginRunner() {}

	@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "unchecked"})
	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(HDRPlugin.class);
		RuneLite.main(args);
	}
}
