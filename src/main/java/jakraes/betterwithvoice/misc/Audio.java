package jakraes.betterwithvoice.misc;

import javax.sound.sampled.AudioFormat;

public class Audio {
	public static final int SAMPLE_RATE = 44100;
	public static final int SAMPLE_SIZE_IN_BITS = 16;
	public static final int CHANNELS = 2;
	public static final boolean SIGNED = true;
	public static final boolean BIG_ENDIAN = true;
	public static final int BUFFER_SIZE = 1024;
	public static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
}
