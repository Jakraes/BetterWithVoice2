package jakraes.betterwithvoice.server;

import jakraes.betterwithvoice.BetterWithVoice;
import jakraes.betterwithvoice.misc.AudioPacket;
import org.jetbrains.annotations.NotNull;
import turniplabs.halplibe.HalpLibe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	private static Server INSTANCE;
	private ServerSocket serverSocket;
	private Thread listenerThread;
	private List<ServerHelper> helperList;

	public static Server getInstance() {
		if (INSTANCE == null && !HalpLibe.isClient) {
			INSTANCE = new Server();
		}

		return INSTANCE;
	}

	private Server() {
		helperList = new ArrayList<>();
	}

	public void initialize(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("Server failed to create server socket.");
			throw new RuntimeException(e);
		}
	}

	public void startListening() {
		listenerThread = new Thread(() -> {
			while (!listenerThread.isInterrupted()) {
				Socket socket;

				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					BetterWithVoice.LOGGER.error("Server failed accept connection.");
					throw new RuntimeException(e);
				}

				ServerHelper helper = new ServerHelper(socket);

				helperList.add(helper);

				helper.start();
			}
		});

		listenerThread.start();
	}

	private class ServerHelper extends Thread {
		private Socket socket;
		private ObjectInputStream inputStream;
		private ObjectOutputStream outputStream;

		public ServerHelper(@NotNull Socket socket) {
			this.socket = socket;

			try {
				inputStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				BetterWithVoice.LOGGER.error("ServerHelper failed to get input stream.");
				throw new RuntimeException(e);
			}

			try {
				outputStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				BetterWithVoice.LOGGER.error("ServerHelper failed to get output stream.");
				throw new RuntimeException(e);
			}
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				AudioPacket packet;

				try {
					packet = (AudioPacket) inputStream.readObject();
				} catch (IOException e) {
					BetterWithVoice.LOGGER.error("ServerHelper failed to read object.");
					throw new RuntimeException(e);
				} catch (ClassNotFoundException e) {
					BetterWithVoice.LOGGER.error("ServerHelper failed to find AudioPacket class.");
					throw new RuntimeException(e);
				}

				for (ServerHelper helper : helperList) {
					try {
						helper.getOutputStream().writeObject(packet);
					} catch (IOException e) {
						BetterWithVoice.LOGGER.error("ServerHelper failed to write object.");
						throw new RuntimeException(e);
					}
				}
			}
		}

		public ObjectOutputStream getOutputStream() {
			return outputStream;
		}
	}
}
