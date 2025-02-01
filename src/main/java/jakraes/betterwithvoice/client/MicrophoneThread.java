package jakraes.betterwithvoice.client;

import jakraes.betterwithvoice.BetterWithVoice;
import jakraes.betterwithvoice.misc.Audio;
import jakraes.betterwithvoice.misc.AudioPacket;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MicrophoneThread extends Thread {
	private ObjectOutputStream outputStream;
	private TargetDataLine microphone;
	private boolean recording;

	public MicrophoneThread(Socket socket) {
		try {
			outputStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("MicrophoneThread failed to get output stream.");
			throw new RuntimeException(e);
		}

		try {
			microphone = AudioSystem.getTargetDataLine(Audio.FORMAT);
		} catch (LineUnavailableException e) {
			BetterWithVoice.LOGGER.error("MicrophoneThread failed to get target data line.");
			throw new RuntimeException(e);
		}

		recording = false;
	}

	public void open() {
		BetterWithVoice.LOGGER.info("MicrophoneThread open().");
		recording = true;
		microphone.start();
	}

	public void close() {
		BetterWithVoice.LOGGER.info("MicrophoneThread close().");
		recording = false;
		microphone.stop();
	}

	public boolean isOpen() {
		return recording;
	}

	@Override
	public void run() {
		try {
			microphone.open();
		} catch (LineUnavailableException e) {
			BetterWithVoice.LOGGER.error("MicrophoneThread failed to open microphone.");
			throw new RuntimeException(e);
		}

		while (!isInterrupted()) {
			if (!recording) continue;

			byte[] buffer = new byte[Audio.BUFFER_SIZE];
			int size = microphone.read(buffer, 0, Audio.BUFFER_SIZE);
			BetterWithVoice.LOGGER.info("MicrophoneThread read {} bytes.", size);

			AudioPacket packet = new AudioPacket(buffer, size);

			try {
				outputStream.writeObject(packet);
			} catch (IOException e) {
				BetterWithVoice.LOGGER.error("MicrophoneThread failed to write object.");
				throw new RuntimeException(e);
			}
		}

		microphone.close();
	}
}
