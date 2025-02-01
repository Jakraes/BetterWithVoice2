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
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("Client failed to close socket.");
			throw new RuntimeException(e);
		}

		stopAudio();

		socket = null;
	}

	public void startAudio() {
		microphoneThread.start();
		speakerThread.start();
	}

	public void stopAudio() {
		microphoneThread.interrupt();
		speakerThread.interrupt();

		microphoneThread = null;
		speakerThread = null;
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
}
