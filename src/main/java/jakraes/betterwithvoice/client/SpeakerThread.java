package jakraes.betterwithvoice.client;

import jakraes.betterwithvoice.BetterWithVoice;
import jakraes.betterwithvoice.misc.Audio;
import jakraes.betterwithvoice.misc.AudioPacket;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class SpeakerThread extends Thread {
	private ObjectInputStream inputStream;
	private SourceDataLine speakers;

	public SpeakerThread(Socket socket) {
		try {
			inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			BetterWithVoice.LOGGER.error("SpeakerThread failed to get input stream.");
			throw new RuntimeException(e);
		}

		try {
			speakers = AudioSystem.getSourceDataLine(Audio.FORMAT);
		} catch (LineUnavailableException e) {
			BetterWithVoice.LOGGER.error("SpeakerThread failed to get source data line.");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		try {
			speakers.open();
		} catch (LineUnavailableException e) {
			BetterWithVoice.LOGGER.error("SpeakerThread failed to open speakers.");
			throw new RuntimeException(e);
		}
		speakers.start();

		while (!isInterrupted()) {
			AudioPacket packet;

			try {
				packet = (AudioPacket) inputStream.readObject();
			} catch (IOException e) {
				BetterWithVoice.LOGGER.error("SpeakerThread failed to read object.");
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				BetterWithVoice.LOGGER.error("SpeakerThread failed to find AudioPacket class.");
				throw new RuntimeException(e);
			}

			BetterWithVoice.LOGGER.info("SpeakerThread read {} bytes", packet.bufferSize);
			speakers.write(packet.buffer, 0, packet.bufferSize);
		}

		speakers.stop();
		speakers.close();
	}
}
