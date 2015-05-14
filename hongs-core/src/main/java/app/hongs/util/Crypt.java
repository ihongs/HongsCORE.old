/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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

    public static String encrypt(String content, String scrtkey) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), scrtkey, false), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public static String decrypt(String content, String scrtkey) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), scrtkey, true ), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw HongsException.common(null, ex);
        }
    }

    public static byte[] encrypt(byte[] content, String scrtkey) throws HongsException {
        return docrypt(content, scrtkey, false);
    }

    public static byte[] decrypt(byte[] content, String scrtkey) throws HongsException {
        return docrypt(content, scrtkey, true );
    }

    private static byte[] docrypt(byte[] content, String scrtkey, boolean decrypt) throws HongsException {
        String enc = CoreConfig.getInstance().getProperty("core.crypto.method", "AES");
        int    len = CoreConfig.getInstance().getProperty("core.crypto.length",  128 );

        try {
            SecretKeySpec kspc;
            KeyGenerator  kgen;
            Cipher        cphr;

            kgen = KeyGenerator.getInstance(enc);
            cphr =       Cipher.getInstance(enc);

            kgen.init(len, new SecureRandom(scrtkey.getBytes(/**/)));
            kspc = new SecretKeySpec(kgen.generateKey().getEncoded(), enc);
            cphr.init(decrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE, kspc);

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
