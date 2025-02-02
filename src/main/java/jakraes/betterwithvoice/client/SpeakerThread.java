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
			AudioPacket packet = new AudioPacket();

			try {
				packet = (AudioPacket) inputStream.readObject();
			} catch (IOException e) {
				BetterWithVoice.LOGGER.warn("SpeakerThread failed to read object.");
				continue;
			} catch (ClassNotFoundException e) {
				BetterWithVoice.LOGGER.error("SpeakerThread failed to find AudioPacket class.");
				throw new RuntimeException(e);
			}

			double distance = calculateDistance(
				packet.sourceX, packet.sourceY, packet.sourceZ,
				Client.getInstance().getX(), Client.getInstance().getY(), Client.getInstance().getZ()
			);

			if (distance > 30) continue;

			float[] volumes = calculateStereoVolumes(
				packet.sourceX, packet.sourceZ,
				Client.getInstance().getX(), Client.getInstance().getZ(),
				(float) Client.getInstance().getyRot(), (float) distance
			);

			byte[] adjustedBuffer = adjustStereoVolume(packet.buffer, volumes[0], volumes[1]);

			speakers.write(adjustedBuffer, 0, packet.bufferSize);
		}

		speakers.stop();
		speakers.close();

		BetterWithVoice.LOGGER.info("SpeakerThread finished.");
	}

	private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
	}

	private float[] calculateStereoVolumes(double sourceX, double sourceZ, double listenerX, double listenerZ, float listenerYaw, float distance) {
		double soundX = sourceX - listenerX;
		double soundZ = sourceZ - listenerZ;

		double listenerYawRad = Math.toRadians(listenerYaw);

		double lookX = -Math.sin(listenerYawRad);
		double lookZ = Math.cos(listenerYawRad);

		double dotProduct = soundX * lookX + soundZ * lookZ;
		double crossProduct = soundX * lookZ - soundZ * lookX;

		double lookDistance = Math.sqrt(lookX * lookX + lookZ * lookZ);
		double cosAngle = dotProduct / (distance * lookDistance);
		double angleDifference = Math.acos(cosAngle);

		float pan = (float) -(Math.signum(crossProduct) * Math.sin(angleDifference));

		float distanceAttenuation = Math.max(0, 1 - (distance / 30));

		float leftGain = ((1 - pan) / 2) * distanceAttenuation;
		float rightGain = ((1 + pan) / 2) * distanceAttenuation;

		leftGain = Math.max(0, Math.min(leftGain, 1));
		rightGain = Math.max(0, Math.min(rightGain, 1));

		return new float[]{leftGain, rightGain};
	}

	private byte[] adjustStereoVolume(byte[] audioBuffer, float leftGain, float rightGain) {
		byte[] adjustedBuffer = new byte[audioBuffer.length];

		for (int i = 0; i < audioBuffer.length; i += 4) {
			short leftSample = (short) ((audioBuffer[i] << 8) | (audioBuffer[i + 1] & 0xFF));
			short rightSample = (short) ((audioBuffer[i + 2] << 8) | (audioBuffer[i + 3] & 0xFF));

			leftSample = (short) (leftSample * leftGain);
			rightSample = (short) (rightSample * rightGain);

			adjustedBuffer[i] = (byte) ((leftSample >> 8) & 0xFF);
			adjustedBuffer[i + 1] = (byte) (leftSample & 0xFF);
			adjustedBuffer[i + 2] = (byte) ((rightSample >> 8) & 0xFF);
			adjustedBuffer[i + 3] = (byte) (rightSample & 0xFF);
		}

		return adjustedBuffer;
	}
}
