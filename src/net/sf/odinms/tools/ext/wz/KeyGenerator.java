

package net.sf.odinms.tools.ext.wz;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
/**
 *
 * @author Danny
 */
public class KeyGenerator {
    public static void main(String[] args) {
		System.out.println("Starting key generation...");
        byte[] iv = new byte[]{	(byte) 0x4d, (byte) 0x23, (byte) 0xc7, (byte) 0x2b,
								(byte) 0x4d, (byte) 0x23, (byte) 0xc7, (byte) 0x2b,
								(byte) 0x4d, (byte) 0x23, (byte) 0xc7, (byte) 0x2b,
								(byte) 0x4d, (byte) 0x23, (byte) 0xc7, (byte) 0x2b,
		};
		byte[] key = new byte[]{(byte) 0x13, 0x00, 0x00, 0x00,
								(byte) 0x08, 0x00, 0x00, 0x00,
								(byte) 0x06, 0x00, 0x00, 0x00,
								(byte) 0xB4, 0x00, 0x00, 0x00,
								(byte) 0x1B, 0x00, 0x00, 0x00,
								(byte) 0x0F, 0x00, 0x00, 0x00,
								(byte) 0x33, 0x00, 0x00, 0x00,
								(byte) 0x52, 0x00, 0x00, 0x00
		};

		System.out.println("Initializing AES Cipher...");

		Cipher cipher = null;
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

		try {
			cipher = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return;
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Cipher initialized.");

		byte[] encKey = new byte[0xFFFF];
		
		System.out.println("Running AES Cipher in OFB mode...");
		for (int i = 0; i < 0xFFFF; i += 16) {
			try {
				iv = cipher.doFinal(iv);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
			int len = 16;
			if (i == 0xFFF0) {
				len = 15;
			}
			System.arraycopy(iv, 0, encKey, i, len);
		}
		System.out.println("Running complete.");
		System.out.println("65535 keys successfully generated.");
		System.out.println("Writing to gms.key ...");
		
		File keysFile = new File("gms.hex");
		
		try {
			keysFile.createNewFile();
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(keysFile));
			dos.write(encKey);
			dos.flush();
			dos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		
		System.out.println("Writing complete.");
		System.out.println("Key generation complete.");
	}
}
