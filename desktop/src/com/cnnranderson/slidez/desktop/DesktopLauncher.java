package com.cnnranderson.slidez.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cnnranderson.slidez.Application;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = Application.TITLE + " v" + Application.VERSION;
		config.width = Application.V_WIDTH;
		config.height = Application.V_HEIGHT;
		config.backgroundFPS = 60;
		config.foregroundFPS = 60;
		//config.resizable = false;
		new LwjglApplication(new Application(), config);
	}
}
