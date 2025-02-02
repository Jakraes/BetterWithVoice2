package jakraes.betterwithvoice.misc;

public class AudioPacket implements java.io.Serializable {
	public byte[] buffer;
	public int bufferSize;
	public double sourceX, sourceY, sourceZ; // Add source position

	public AudioPacket(byte[] buffer, int bufferSize) {
		this.buffer = buffer;
		this.bufferSize = bufferSize;
	}

	public AudioPacket() {
		// Default constructor for deserialization
	}
}
