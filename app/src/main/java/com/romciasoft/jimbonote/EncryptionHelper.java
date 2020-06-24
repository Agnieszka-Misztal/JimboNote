package com.romciasoft.jimbonote;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class EncryptionHelper {

    //generowania hasha dla hasła - użycie md5
    public static String getHashedString2(String passwordToHash, String salt)
    {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    //bardziej bezpieczne generowania hasha dla hasła - użycie PBKDF2WithHmacSHA1
    //więcej info - https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/#PBKDF2WithHmacSHA1
    public static String getSecureHash(String password,String salt)
    {
        try
        {
            //ilość iteracji podczas generowania hasha
            int iterations = 1000;

            //hasło oraz salt
            char[] chars = password.toCharArray();
            byte[] saltBytes = salt.getBytes("UTF-8");

            PBEKeySpec spec = new PBEKeySpec(chars, saltBytes, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.encodeToString(hash,Base64.NO_WRAP);
        }catch (Exception ex)
        {
            System.out.println("getSecureHash: " + ex.toString());
            return "";
        }
    }

}
