package app.hongs.util;

import app.hongs.CoreConfig;
import app.hongs.HongsException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密解密
 * @author Hongs
 */
public class Crypt {

    public static String encrypt(String content) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), null, false), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public static String decrypt(String content) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), null, true ), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public static String encrypt(String content, String key) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), key , false), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public static String decrypt(String content, String key) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), key , true ), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public static byte[] encrypt(byte[] content) throws HongsException {
        return docrypt(content, null, false);
    }

    public static byte[] decrypt(byte[] content) throws HongsException {
        return docrypt(content, null, true );
    }

    public static byte[] encrypt(byte[] content, String key) throws HongsException {
        return docrypt(content, key , false);
    }

    public static byte[] decrypt(byte[] content, String key) throws HongsException {
        return docrypt(content, key , true );
    }

   private static byte[] docrypt(byte[] content, String key, boolean dec) throws HongsException {
        CoreConfig conf = CoreConfig.getInstance();
        if  (  key == null  ) {
               key = conf.getProperty("core.crypt.seckey", "HCJ");
        }
        String enc = conf.getProperty("core.crypt.method", "AES");
        int    len = conf.getProperty("core.crypt.length",  128 );

        try {
            SecretKeySpec kspc;
            KeyGenerator  kgen;
            Cipher        cphr;

            kgen = KeyGenerator.getInstance(enc);
            cphr =       Cipher.getInstance(enc);

            kgen.init(len, new SecureRandom(key.getBytes()));
            kspc = new SecretKeySpec(kgen.generateKey().getEncoded(), enc);
            cphr.init(dec? Cipher.DECRYPT_MODE: Cipher.ENCRYPT_MODE, kspc);

            return cphr.doFinal(content);
        } catch (NoSuchAlgorithmException e) {
            throw HongsException.common(null, e);
        } catch (NoSuchPaddingException e) {
            throw HongsException.common(null, e);
        } catch (InvalidKeyException e) {
            throw HongsException.common(null, e);
        } catch (IllegalBlockSizeException e) {
            throw HongsException.common(null, e);
        } catch (BadPaddingException e) {
            throw HongsException.common(null, e);
        }
    }

}
