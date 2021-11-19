import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.security.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import ove.crypto.digest.Blake2b;

public class Utils {

	static String toHexString(byte[] bytes) {
		if(bytes==null || bytes.length==0) return "";
		final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
	    byte[] hexChars = new byte[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars, StandardCharsets.UTF_8);
	}
	
	static byte[] toBytesArray(String str) throws DecoderException {
		return Hex.decodeHex(str.toCharArray());
		// str.getBytes() ?
	}

	static byte[] toBytesArray(byte b) throws DecoderException {
		byte[] bytesArray = {b};
		return bytesArray;
	}

	static byte[] concat_hash(byte[] hash1, byte[] hash2) throws IOException { //Ex 1
		Blake2b.Param param = new Blake2b.Param().setDigestLength(32);
		Blake2b blake2b = Blake2b.Digest.newInstance(param);        
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(hash1);
		outputStream.write(hash2);
		byte twoHashes[] = outputStream.toByteArray();
		return blake2b.digest(twoHashes);
	}	
}