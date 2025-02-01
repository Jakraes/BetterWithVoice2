package jakraes.betterwithvoice.misc;

import java.io.Serializable;

public class AudioPacket implements Serializable {
	public byte[] buffer;
	public int bufferSize;
	public int x, y, z;

	public AudioPacket(byte[] buffer, int bufferSize) {
		this.buffer = buffer;
		this.bufferSize = bufferSize;
		x = 0;
		y = 0;
		z = 0;
	}
}
