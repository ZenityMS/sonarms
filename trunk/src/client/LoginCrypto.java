package client;

import java.security.MessageDigest;
import java.util.Random;
import tools.HexTool;

/**
 * 
 * @author Frz
 */
public class LoginCrypto {
    private static String toSimpleHexString(byte[] bytes) {
        return HexTool.toString(bytes).replace(" ", "").toLowerCase();
    }

    private static String hashWithDigest(String in, String digest) {
        try {
            MessageDigest Digester = MessageDigest.getInstance(digest);
            Digester.update(in.getBytes("UTF-8"), 0, in.length());
            byte[] sha1Hash = Digester.digest();
            return toSimpleHexString(sha1Hash);
        } catch (Exception e) {
            throw new RuntimeException("Encoding the string failed", e);
        }
    }

    public static String hexSha1(String in) {
        return hashWithDigest(in, "SHA-1");
    }

    public static boolean checkSha1Hash(String hash, String password) {
        return hash.equals(hexSha1(password));
    }

    public static boolean checkSaltedSha512Hash(String hash, String password, String salt) {
        return hash.equals(makeSaltedSha512Hash(password, salt));
    }

    public static String makeSaltedSha512Hash(String password, String salt) {
        return hashWithDigest(password + salt, "SHA-512");
    }

    public static String makeSalt() {
        byte[] salt = new byte[16];
        new Random().nextBytes(salt);
        return toSimpleHexString(salt);
    }
}
