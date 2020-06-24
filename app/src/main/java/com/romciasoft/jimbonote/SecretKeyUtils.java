package com.romciasoft.jimbonote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SecretKeyUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void CheckAndGeneratePassword()
    {
        try {
            SecretKey key = getSecretKey();
            if (key == null)
            {
                generateSecretKey(new KeyGenParameterSpec.Builder(
                        "JiminRomkaSecretKey",
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
        } catch (Exception e)
        {
            System.out.println("CheckAndGeneratePassword: " + e.toString());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    public static SecretKey getSecretKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        return ((SecretKey)keyStore.getKey("JiminRomkaSecretKey", null));
    }

    public static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    public static String encryptMessage(String message, Context context)
    {
        try {


            Cipher cipher = getCipher();
            SecretKey secretKey = getSecretKey();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();

            SharedPreferences sharedPref = context.getSharedPreferences("test", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString("ivi", Base64.encodeToString(iv, Base64.DEFAULT));
            editor.commit();

            byte[] encryptedInfo = cipher.doFinal(message.getBytes("UTF-8"));

            return Base64.encodeToString(encryptedInfo, Base64.NO_WRAP);
        }catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }

        return "";
    }

    public static String decryptMessage(String message, Context context)
    {
        try {

            SharedPreferences sharedPref = context.getSharedPreferences("test",Context.MODE_PRIVATE);
            String ivArray = sharedPref.getString("ivi","");
            byte[] iv = Base64.decode(ivArray, Base64.DEFAULT);

            Cipher cipher = getCipher();
            SecretKey secretKey = getSecretKey();
            cipher.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
            return new String(cipher.doFinal(Base64.decode(message,Base64.NO_WRAP)));
        }catch (Exception e)
        {
            System.out.println("Error while encrypting: " + e.toString());
        }

        return "";
    }


}

