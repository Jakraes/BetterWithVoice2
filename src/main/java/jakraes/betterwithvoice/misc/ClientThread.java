package jakraes.betterwithvoice.misc;

import jakraes.betterwithvoice.BetterWithVoice;

import java.io.IOException;
import java.net.Socket;

public class ClientThread implements Runnable {
	private Socket socket;

	public ClientThread(String host, int port) throws IOException {
		this.socket = new Socket(host, port);
		BetterWithVoice.LOGGER.info("Connected to server thread.");
	}

	@Override
	public void run() {

	}
}
