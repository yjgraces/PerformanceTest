package com.yongche.component.groundhog.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

//import android.annotation.SuppressLint;
import common.Base64;

import com.yongche.component.groundhog.push.CommonConfig;

public class SetSecretKeyRequestMessage extends RequestMessage {
    public String publicKey;
    public byte[] secretKey;
    
    public SetSecretKeyRequestMessage() {
        this.functionId = FUNCTION_ID_SET_SECRET_KEY;
    }
    
    @Override
    public byte[] encode() throws GroundhogMessageException {        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);

        try {
            byte[] data = this.encryptSecretKey();
            int length = data.length;
            out.write(data, 0, length);
            out.flush();
        } catch (Exception e) {
            throw new GroundhogMessageException(e);
        }
        
        return byteStream.toByteArray();
    }
    
    private byte[] encryptSecretKey() throws Exception {
        try {
            String content = this.publicKey;
            
            if( this.publicKey.contains("-----BEGIN PUBLIC KEY-----") ) {
              content = this.publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                              .replace("-----END PUBLIC KEY-----", "");
            }
            byte[] buffer = Base64.decode(content,Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(CommonConfig.KEY_FACTORY_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            Key publicKey = keyFactory.generatePublic(keySpec);
            
            byte[] encryptContent = new byte[128];
            System.arraycopy(this.secretKey, 0, encryptContent, 0, this.secretKey.length);

            Cipher cipher = Cipher.getInstance(CommonConfig.KEY_ENCRYPT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(encryptContent);
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("No such algorithm");  
        } catch (InvalidKeySpecException e) {  
            throw new Exception("Invalid public key");  
        }catch (NullPointerException e) {  
            throw new Exception("Public key is empty");  
        }
    }
}
