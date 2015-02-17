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
public class Crypto {

    public static String encrypt(String content, String scrtkey) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), scrtkey, false), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public static String decrypt(String content, String scrtkey) throws HongsException {
        try {
            return new String(docrypt(content.getBytes(), scrtkey, true ), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new HongsException(HongsException.COMMON, ex);
        }
    }

    public static byte[] encrypt(byte[] content, String scrtkey) throws HongsException {
        return docrypt(content, scrtkey, false);
    }

    public static byte[] decrypt(byte[] content, String scrtkey) throws HongsException {
        return docrypt(content, scrtkey, true );
    }

    private static byte[] docrypt(byte[] content, String scrtkey, boolean decrypt) throws HongsException {
        String enc = CoreConfig.getInstance().getProperty("core.symmetric.encryption.method", "AES");
        int    len = CoreConfig.getInstance().getProperty("core.symmetric.encryption.length",  128 );

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
            throw new HongsException(HongsException.COMMON, e);
        } catch (NoSuchPaddingException e) {
            throw new HongsException(HongsException.COMMON, e);
        } catch (InvalidKeyException e) {
            throw new HongsException(HongsException.COMMON, e);
        } catch (IllegalBlockSizeException e) {
            throw new HongsException(HongsException.COMMON, e);
        } catch (BadPaddingException e) {
            throw new HongsException(HongsException.COMMON, e);
        }
    }

}
