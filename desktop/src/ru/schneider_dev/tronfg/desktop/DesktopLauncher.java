package ru.schneider_dev.tronfg.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ru.schneider_dev.tronfg.GameCallback;
import ru.schneider_dev.tronfg.TRONgame;

public class DesktopLauncher {
	public DesktopLauncher() {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new TRONgame(callback), config);
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
