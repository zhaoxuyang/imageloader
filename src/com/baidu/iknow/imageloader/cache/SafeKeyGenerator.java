package com.baidu.iknow.imageloader.cache;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 生成sdcache 存储的键值
 * 
 * @author zhaoxuyang
 * @since 2015-10-12
 */
public class SafeKeyGenerator {
    
    private static final char[] HEX_CHAR_ARRAY = "0123456789abcdef".toCharArray();
    // 32 bytes from sha-256 -> 64 hex chars.
    private static final char[] SHA_256_CHARS = new char[64];

    private final LruCache<String, String> loadIdToSafeHash = new LruCache<String, String>(1000);

    private static String calculateHexStringDigest(String key) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(key.getBytes());
            return sha256BytesToHex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSafeKey(String key) {
        String safeKey;
        synchronized (loadIdToSafeHash) {
            safeKey = loadIdToSafeHash.get(key);
        }
        if (safeKey == null) {
            safeKey = calculateHexStringDigest(key);
        }
        synchronized (loadIdToSafeHash) {
            loadIdToSafeHash.put(key, safeKey);
        }
        return safeKey;
    }

    /**
     * Returns the hex string of the given byte array representing a SHA256
     * hash.
     */
    public static String sha256BytesToHex(byte[] bytes) {
        synchronized (SHA_256_CHARS) {
            return bytesToHex(bytes, SHA_256_CHARS);
        }
    }

    // Taken from:
    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    // /9655275#9655275
    private static String bytesToHex(byte[] bytes, char[] hexChars) {
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_CHAR_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
