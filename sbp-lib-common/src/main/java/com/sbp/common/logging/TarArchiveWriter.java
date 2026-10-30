package com.sbp.common.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/**
 * Minimal ustar tar writer (no external dependencies).
 */
final class TarArchiveWriter implements AutoCloseable {

	private static final int BLOCK = 512;

	private final OutputStream out;

	TarArchiveWriter(OutputStream out) {
		this.out = out;
	}

	void addFile(Path file) throws IOException {
		byte[] content = Files.readAllBytes(file);
		String name = file.getFileName().toString();
		writeHeader(name, content.length);
		out.write(content);
		padToBlock(content.length);
	}

	@Override
	public void close() throws IOException {
		out.write(new byte[BLOCK * 2]);
		out.flush();
	}

	private void writeHeader(String name, long size) throws IOException {
		byte[] header = new byte[BLOCK];
		putAscii(header, 0, name, 100);
		putOctal(header, 100, 644, 8);
		putOctal(header, 108, 0, 8);
		putOctal(header, 116, 0, 8);
		putOctal(header, 124, size, 12);
		putOctal(header, 136, System.currentTimeMillis() / 1000, 12);
		for (int i = 148; i < 156; i++) {
			header[i] = (byte) ' ';
		}
		header[156] = '0';
		putAscii(header, 257, "ustar", 6);
		putAscii(header, 263, "00", 2);
		int checksum = 0;
		for (byte b : header) {
			checksum += b & 0xFF;
		}
		putOctal(header, 148, checksum, 7);
		header[155] = 0;
		out.write(header);
	}

	private void padToBlock(long size) throws IOException {
		int rem = (int) (size % BLOCK);
		if (rem != 0) {
			out.write(new byte[BLOCK - rem]);
		}
	}

	private static void putAscii(byte[] buf, int offset, String value, int max) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		int len = Math.min(bytes.length, max - 1);
		System.arraycopy(bytes, 0, buf, offset, len);
	}

	private static void putOctal(byte[] buf, int offset, long value, int width) {
		String octal = Long.toOctalString(value);
		int start = offset + width - octal.length() - 1;
		for (int i = offset; i < start; i++) {
			buf[i] = '0';
		}
		for (int i = 0; i < octal.length(); i++) {
			buf[start + i] = (byte) octal.charAt(i);
		}
		buf[offset + width - 1] = 0;
	}

}
