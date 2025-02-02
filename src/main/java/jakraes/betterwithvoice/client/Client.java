package jakraes.betterwithvoice.client;

import jakraes.betterwithvoice.BetterWithVoice;
import turniplabs.halplibe.HalpLibe;

import java.io.IOException;
import java.net.Socket;

public class Client {
	private static Client INSTANCE;
	private Socket socket;
	private MicrophoneThread microphoneThread;
	private SpeakerThread speakerThread;
	private double x;
	private double y;
	private double z;
	private double xRot;
	private double yRot;

	public static Client getInstance() {
		if (INSTANCE == null && HalpLibe.isClient) {
			INSTANCE = new Client();
		}

		return INSTANCE;
	}

	public void connect(String host, int port) {
		try {
			socket = new Socket(host, port);
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("Client failed to connect to host.");
			throw new RuntimeException(e);
		}

		microphoneThread = new MicrophoneThread(socket);
		speakerThread = new SpeakerThread(socket);

		BetterWithVoice.LOGGER.info("Client connected.");
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			BetterWithVoice.LOGGER.warn("Client failed to close socket.");
		}

		stopAudio();

		socket = null;

		BetterWithVoice.LOGGER.info("Client disconnected.");
	}

	public void startAudio() {
		microphoneThread.start();
		speakerThread.start();
		BetterWithVoice.LOGGER.info("Client audio started.");
	}

	public void stopAudio() {
		microphoneThread.interrupt();
		speakerThread.interrupt();

		microphoneThread = null;
		speakerThread = null;

		BetterWithVoice.LOGGER.info("Client audio stopped.");
	}

	public void startRecording() {
		microphoneThread.open();
	}

	public void stopRecording() {
		microphoneThread.close();
	}

	public boolean isRecording() {
		return microphoneThread.isOpen();
	}

	public boolean isDisconnected() {
		return socket == null || socket.isClosed();
	}

	public void setPos(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setRot(double xRot, double yRot) {
		this.xRot = xRot;
		this.yRot = yRot;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double getxRot() {
		return xRot;
	}

	public double getyRot() {
		return yRot;
	}
}
