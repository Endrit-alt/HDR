package com.endrit.hdr;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HDRPluginTest {
	@Test
	public void reloadsWhenPlaneChangesInsideCox() {
		assertTrue(HDRPlugin.shouldReloadOnSceneTransition(
				HDRPlugin.AreaToggle.COX,
				HDRPlugin.AreaToggle.COX,
				0,
				1));
	}

	@Test
	public void doesNotReloadForPlaneChangesInOpenWorld() {
		assertFalse(HDRPlugin.shouldReloadOnSceneTransition(
				HDRPlugin.AreaToggle.OPEN_WORLD,
				HDRPlugin.AreaToggle.OPEN_WORLD,
				0,
				1));
	}

	@Test
	public void retainsOlmRopeReloadOnSamePlane() {
		assertTrue(HDRPlugin.shouldReloadOnSceneTransition(
				HDRPlugin.AreaToggle.COX,
				HDRPlugin.AreaToggle.COX_OLM,
				0,
				0));
	}

	@Test
	public void doesNotReloadWhenAreaAndPlaneAreUnchanged() {
		assertFalse(HDRPlugin.shouldReloadOnSceneTransition(
				HDRPlugin.AreaToggle.COX,
				HDRPlugin.AreaToggle.COX,
				1,
				1));
	}
}
