package com.yongche.component.groundhog.message;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.yongche.component.groundhog.IMessage;
import com.yongche.component.groundhog.IParser;
import com.yongche.component.groundhog.push.CommonConfig;
//import com.yongche.component.groundhog.push.Logger;

public class GroundhogParser implements IParser {
    static public final byte[] MAGIC_NUMBER = {0x23, 0x23, 0x23, 0x23};
    static public final int HEADER_LENGTH = 22;

    private SecretKeyFactory keyFactory;
    private Cipher cipher;
    private IvParameterSpec ivParameters;
    private DESedeKeySpec desedeKeySpec;
    private SecretKey secretKey;
    
    public GroundhogParser() {
        try {
            // Get the secret key factor for generating DESede keys
            this.keyFactory = SecretKeyFactory.getInstance(CommonConfig.KEY_GENERATE_ALGORITHM);
            
            // Create a DESede Cipher
            this.cipher = Cipher.getInstance(CommonConfig.DES_ENCRYPT_ALGORITHM);
            
            this.ivParameters = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSecretKey(byte[] secretKey) {
        try {
            // Create a DESede key spec from the key
            this.desedeKeySpec = new DESedeKeySpec(secretKey);
        
            // Generate a DESede SecretKey object
            this.secretKey = keyFactory.generateSecret(desedeKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public synchronized void sendMsg(IMessage msg, DataOutputStream dataOut) throws IOException, GroundhogMessageException {
        if (!(msg instanceof GroundhogMessage)) {
            throw new GroundhogMessageException("It is not a GroundhogMessage");
        }
        
        GroundhogMessage message = (GroundhogMessage) msg;
        
        dataOut.write(MAGIC_NUMBER);
        dataOut.writeShort(message.messageType);
        dataOut.writeByte(message.functionId);
        dataOut.writeByte(message.status);
        dataOut.writeLong(message.sequenceId);
        
        int bodyLength = 0;
        byte[] messageBytes = message.encode();
        
        if (messageBytes != null) {
            bodyLength = messageBytes.length;
            
            // compression
            if (((message.functionId == GroundhogMessage.FUNCTION_ID_MESSAGE || 
                    message.functionId == GroundhogMessage.FUNCTION_ID_RPC) && 
                    (message.flags & GroundhogMessage.FLAGS_COMPRESS) != 0 &&
                    bodyLength > CommonConfig.BODY_SIZE_THRESHOLD) || 
                    (message.functionId != GroundhogMessage.FUNCTION_ID_MESSAGE && 
                    message.functionId != GroundhogMessage.FUNCTION_ID_RPC &&
                    CommonConfig.COMPRESSION_ENABLE && bodyLength > CommonConfig.BODY_SIZE_THRESHOLD))
            {
                messageBytes = this.compress(messageBytes);
                message.flags |= GroundhogMessage.FLAGS_COMPRESS;
            } else {
                message.flags &= ~GroundhogMessage.FLAGS_COMPRESS;
            }
            
            bodyLength = messageBytes.length;
            
            // encryption
            if (message.functionId == GroundhogMessage.FUNCTION_ID_LOGIN) {
                message.flags |= GroundhogMessage.FLAGS_ENCRYPT;
            }
            
            if (!CommonConfig.ENCRYPTION_ENABLE) {
                switch (message.functionId) {
                    case GroundhogMessage.FUNCTION_ID_MESSAGE:
                    case GroundhogMessage.FUNCTION_ID_RPC:
                        message.flags &= ~GroundhogMessage.FLAGS_ENCRYPT;
                        break;
                    default:
                        break;
                }
            }

            byte[] encrypted = messageBytes;
            if ((message.flags & GroundhogMessage.FLAGS_ENCRYPT) != 0) {
                encrypted = this.encrypt(messageBytes);
                bodyLength = encrypted.length;
            }

            dataOut.writeShort(message.flags);
            dataOut.writeInt(bodyLength);
            dataOut.write(encrypted, 0, bodyLength);
            dataOut.flush();
            
            //Logger.info(this.getClass().getName(), bytesToHex(encrypted));
        } else {
            dataOut.writeShort(message.flags);
            dataOut.writeInt(bodyLength);
            dataOut.flush();
        }
        return;
    }

    @Override
    public GroundhogMessage getMsg(DataInputStream dataIn) throws IOException, GroundhogMessageException {
        byte[] magicNumber = new byte[4];
        dataIn.readFully(magicNumber);
        if (!Arrays.equals(MAGIC_NUMBER, magicNumber)) {
            throw new GroundhogMessageException("The Message magic number is invalid");
        }
        
        short messageType = dataIn.readShort();
        int functionId = dataIn.readByte();
        
        GroundhogMessage message = createMessage(functionId);
        if (message == null) {
            throw new GroundhogMessageException("The Message is not Known");
        }

        message.messageType = messageType;
        message.status = dataIn.readByte();
        message.sequenceId = dataIn.readLong();
        message.flags = dataIn.readShort();

        int bodyLength = dataIn.readInt();
        if (bodyLength > 0) {
            byte[] msgBody = new byte[bodyLength];
            dataIn.readFully(msgBody);
        
            byte[] body = msgBody;
        
            // decryption
            if ((message.flags & GroundhogMessage.FLAGS_ENCRYPT) != 0) {
                body = this.decrypt(msgBody);
            }

            // decompression
            if ((message.flags & GroundhogMessage.FLAGS_COMPRESS) != 0) {
                body = this.decompress(body);
            }
        
            message.decode(body, message.status);
        }
        
        return message;
    }

    private GroundhogMessage createMessage(int functionId) {
        GroundhogMessage message = null;
        switch(functionId) {
        case GroundhogMessage.FUNCTION_ID_ACK:
            message = new AcknowledgeMessage(); 
            break;
        case GroundhogMessage.FUNCTION_ID_LOGIN_RESPONSE:
            message = new LoginResponseMessage(); 
            break;
        case GroundhogMessage.FUNCTION_ID_ASSGIN_WORKER:
            message = new AssignWorkerResponseMessage(); 
            break;
        case GroundhogMessage.FUNCTION_ID_MESSAGE:
            message = new PushRequestMessage(); 
            break;
        case GroundhogMessage.FUNCTION_ID_PING:
            message = new PingMessage(); 
            break;
        case GroundhogMessage.FUNCTION_ID_GET_PUBLIC_KEY_RESPONSE:
            message = new GetPublicKeyResponseMessage();
            break;
        case GroundhogMessage.FUNCTION_ID_RPC_RESPONSE:
            message = new RpcResponseMessage();
            break;
        default:
        }
        return message;
    }
    
    private byte[] encrypt(byte[] content) {
        byte[] encrypted = null;
        
        try {
            // Initialize the cipher and put it into encrypt mode
            this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, this.ivParameters);
            
            // Encrypt the data
            encrypted = this.cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encrypted;
    }
    
    private byte[] decrypt(byte[] content) {
        byte[] decrypted = null;
        
        try {
            // Initialize the cipher and put it into encrypt mode
            this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey, this.ivParameters);

            // Decrypt the data
            decrypted = this.cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //Logger.info(this.getClass().getName(), "decrypted message is " + new String(decrypted));
        return decrypted;
    }

    private byte[] decompress(byte[] data) {
        byte[] output = new byte[0];
        Inflater decompresser = new Inflater();

        //decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        byte[] compressBuffer = new byte[GroundhogMessage.COMPRESS_BUFFER_SIZE];

        try {
            while (!decompresser.finished()) {
                int i = decompresser.inflate(compressBuffer);
                o.write(compressBuffer, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            decompresser.end();
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Logger.info(this.getClass().getName(), "decompress message is " + new String(output));
        return output;
    }
    
    private byte[] compress(byte[] data) {
        byte[] output = new byte[0];
        Deflater compresser = new Deflater();

        //compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        byte[] compressBuffer = new byte[GroundhogMessage.COMPRESS_BUFFER_SIZE];

        try {
            while (!compresser.finished()) {
                int i = compresser.deflate(compressBuffer);
                bos.write(compressBuffer, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            compresser.end();
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;
    }
    
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
