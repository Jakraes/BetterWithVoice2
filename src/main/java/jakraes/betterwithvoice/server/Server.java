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

		BetterWithVoice.LOGGER.info("Server socket initialized.");
	}

	public void startListening() {
		BetterWithVoice.LOGGER.info("Server listening.");

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
				BetterWithVoice.LOGGER.info("Server launched ServerHelper.");
			}
		});

		listenerThread.start();
	}

	private synchronized void removeHelper(ServerHelper helper) {
		helperList.remove(helper);
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
				try {
					socket.close();
				} catch (IOException ex) {
					BetterWithVoice.LOGGER.error("ServerHelper failed to close socket.");
					throw new RuntimeException(ex);
				}
				throw new RuntimeException(e);
			}

			try {
				outputStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				BetterWithVoice.LOGGER.error("ServerHelper failed to get output stream.");
				try {
					socket.close();
				} catch (IOException ex) {
					BetterWithVoice.LOGGER.error("ServerHelper failed to close socket.");
					throw new RuntimeException(ex);
				}
				throw new RuntimeException(e);
			}
		}

		@Override
		public void run() {
			while (!socket.isClosed()) {
				AudioPacket packet = new AudioPacket();

				try {
					packet = (AudioPacket) inputStream.readObject();
				} catch (IOException e) {
					BetterWithVoice.LOGGER.warn("ServerHelper failed to read object.");
					closeSocket();
				} catch (ClassNotFoundException e) {
					BetterWithVoice.LOGGER.error("ServerHelper failed to find AudioPacket class.");
					closeSocket();
					throw new RuntimeException(e);
				}

				for (ServerHelper helper : helperList) {
					if (helper == this) continue;

					try {
						helper.getOutputStream().writeObject(packet);
					} catch (IOException e) {
						BetterWithVoice.LOGGER.warn("ServerHelper failed to write object.");
					}
				}
			}

			removeHelper(this);

			BetterWithVoice.LOGGER.info("ServerHelper finished.");
		}

		public ObjectOutputStream getOutputStream() {
			return outputStream;
		}

		private void closeSocket() {
			try {
				socket.close();
			} catch (IOException e) {
				BetterWithVoice.LOGGER.error("ServerHelper failed to close socket.");
			}
		}
	}
}
