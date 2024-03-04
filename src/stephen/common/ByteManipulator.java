/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.common;

import java.io.UnsupportedEncodingException;

/**
 * This class provides basic APIs to facilitate data format conversions.
 * 
 * @author Stephen Liu
 * 
 */
public class ByteManipulator {

	/**
	 * Convert int value into one byte array.
	 * 
	 * @param intval int value.
	 * @return bytes array
	 */
	public static final byte[] intTo1byte(int intval) {
		byte[] bbytes = new byte[1];
		bbytes[0] = (byte) (intval & 0xFF);
		return bbytes;
	}

	/**
	 * Convert int value into two bytes array.
	 * 
	 * @param intval int value
	 * @return bytes array
	 */
	public static final byte[] intTo2bytes(int intval) {
		byte[] bbytes = new byte[2];
		bbytes[0] = (byte) ((intval >> 8) & 0xFF); // high
		// byte
		bbytes[1] = (byte) (intval & 0xFF); // low byte

		return bbytes;
	}

	/**
	 * Convert int value into four bytes array.
	 * 
	 * @param intval --int value
	 * @return bytes array
	 */
	public static final byte[] intTo4bytes(int intval) {
		byte[] bbytes = new byte[4];
		bbytes[0] = (byte) ((intval >> 24) & 0x000000FF); // highest
		// byte
		bbytes[1] = (byte) ((intval >> 16) & 0x000000FF);
		bbytes[2] = (byte) ((intval >> 8) & 0x000000FF);
		bbytes[3] = (byte) (intval & 0x000000FF); // lowest byte

		return bbytes;
	}

	/**
	 * Convert bytes array to int value. if the bytes length is bigger than 4, only
	 * convert the first 4 bytes and other parts will be ignored.
	 * 
	 * @param b bytes array
	 * @return int value
	 */
	public static int bytesToInt(byte... b) {
		return bytesToInt(b, 0, b.length);

	}

	/**
	 * Convert bytes sub array to int value. if the bytes length is bigger than 4,
	 * only convert the first 4 bytes.
	 * 
	 * @param b      --byte array
	 * @param offset the offset of byte array
	 * @param length the byte length in byte array using for conversion.
	 * @return int value
	 */
	public static int bytesToInt(byte[] b, int offset, int length) {

		int validLen = (length < 4 ? length : 4);

		switch (validLen) {
		case 1:
			return b[offset] & 0xFF;
		case 2:
			return (b[offset] << 8 & 0xFF00) | (b[offset + 1] & 0x00FF);
		case 3:
			return (b[offset] << 16 & 0xFF0000) | (b[offset + 1] << 8 & 0x00FF00) | (b[offset + 2] & 0x0000FF);
		case 4:
			return (b[offset] << 24 & 0xFF000000) | (b[offset + 1] << 16 & 0x00FF0000)
					| (b[offset + 2] << 8 & 0x0000FF00) | (b[offset + 3] & 0x000000FF);
		default:
			return 0;

		}
	}

	/**
	 * Convert string encoded with specific CHARSET to byte array.
	 * 
	 * @param source  a string.
	 * @param charset CHARSET of the parameter string <code>source</code>.
	 * @return byte array corresponding to a string.
	 */
	public static byte[] stringToBytes(String source, String charset) {
		if (source == null) {
			return null;
		}

		try {
			String strTrimmed = source.trim();
			return strTrimmed.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Convert byte array to a string encoded with specific CHARSET.
	 * 
	 * @param source  byte array which will be converted to a string.
	 * @param charset string CHARSET when transforming byte array.
	 * @return a string encoded with the specific CHARSET, corresponding the byte
	 *         array.
	 */
	public static String bytesToString(byte[] source, String charset) {
		return bytesToString(source, 0, source.length, charset);
	}

	/**
	 * Convert byte sub array to a string encoded with specific CHARSET.The string
	 * will be terminated at the first byte '0x00'(null) in the array and the rest
	 * of bytes will be ignored.
	 * 
	 * @param source  byte sub array which will be converted to a string.
	 * @param offset  start position of the byte sub array.
	 * @param length  length of the byte sub array.
	 * @param charset string CHARSET.
	 * @return a string encoded with the specific CHARSET, corresponding the byte
	 *         sub array.
	 */
	public static String bytesToString(byte[] source, int offset, int length, String charset) {
		if (source == null) {
			return null;
		}

		// look for the byte '0x00',which is the end flag of the string, in the
		// byte sub array
		int effectiveLength = length;
		for (int idx = 0; idx < length; idx++) {
			if (source[offset + idx] == 0) {
				effectiveLength = idx + 1;
				break;
			}
		}

		try {
			String str = new String(source, offset, effectiveLength, charset);
			return str.trim();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
