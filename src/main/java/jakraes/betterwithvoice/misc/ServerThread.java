package jakraes.betterwithvoice.misc;


import jakraes.betterwithvoice.BetterWithVoice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
	private class ServerHelperThread implements Runnable {
		private final Socket clientSocket;

		public ServerHelperThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			BetterWithVoice.LOGGER.info("Starting helper thread for {}.", this.clientSocket.getInetAddress());
		}
	}

	private ServerSocket serverSocket;

	public ServerThread(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		BetterWithVoice.LOGGER.info("Started server thread on port {}.", port);
	}

	@Override
	public void run() {
		BetterWithVoice.LOGGER.info("Server thread listening.");

		while (true) {
			Socket clientSocket = null;

			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			BetterWithVoice.LOGGER.info("Connection from {}", clientSocket.getInetAddress());

			new Thread(new ServerHelperThread(clientSocket)).start();
		}
	}
}

