package ru.schneider_dev.tronfg.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import ru.schneider_dev.tronfg.GameCallback;
import ru.schneider_dev.tronfg.TRONgame;

public class DesktopLauncher {
	public DesktopLauncher() {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("TRONFG");
		config.setWindowedMode(800, 480);
		config.useVsync(true);
		config.setForegroundFPS(60);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
		
		new Lwjgl3Application(new TRONgame(callback), config);
	}

	private GameCallback callback = new GameCallback() {
		@Override
		public void sendMessage(int message) {
			System.out.println("DesktopLauncher sendMessage: " + message);
		}
	};

	public static void main(String[] args) {
		new DesktopLauncher();
	}
}
