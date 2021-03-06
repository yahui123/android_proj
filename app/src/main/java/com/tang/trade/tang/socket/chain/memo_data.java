package com.tang.trade.tang.socket.chain;


import com.google.common.primitives.UnsignedLong;
import com.tang.trade.tang.socket.bitlib.bitcoinj.Base58;
import com.tang.trade.tang.socket.fc.crypto.aes;
import com.tang.trade.tang.socket.fc.crypto.sha224_object;
import com.tang.trade.tang.socket.fc.crypto.sha256_object;
import com.tang.trade.tang.socket.fc.crypto.sha512_object;
import com.tang.trade.tang.socket.fc.io.raw_type;
import com.tang.trade.tang.socket.private_key;
import com.tang.trade.tang.socket.public_key;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class memo_data {
    public static class memo_message {
        public int checksum;
        public String text;
        public memo_message(int nChecksum, String strText) {
            checksum = nChecksum;
            text = strText;
        }

        public ByteBuffer serialize() {
            raw_type rawType = new raw_type();
            byte[] bytesChecksum = rawType.get_byte_array(checksum);
            byte[] bytesContent = text.getBytes(Charset.forName("UTF-8"));

            ByteBuffer byteBuffer = ByteBuffer.allocate(bytesContent.length + 4);
            byteBuffer.put(bytesChecksum);
            byteBuffer.put(bytesContent);

            return byteBuffer;
        }

        public static memo_message deserialize(ByteBuffer byteBuffer) {
            byte[] byteSerial = byteBuffer.array();
            String strBuffer = new String(byteSerial, 4, byteSerial.length - 4, Charset.forName("UTF-8"));

            byte[] byteChecksum = new byte[4];
            System.arraycopy(byteSerial, 0, byteChecksum, 0, byteChecksum.length);

            raw_type rawType = new raw_type();
            return new memo_message(rawType.byte_array_to_int(byteChecksum), strBuffer);
        }
    }

    public types.public_key_type from;
    public types.public_key_type to;
    /**
     * 64 bit nonce format:
     * [  8 bits | 56 bits   ]
     * [ entropy | timestamp ]
     * Timestamp is number of microseconds since the epoch
     * Entropy is a byte taken from the hash of a new private key
     *
     * This format is not mandated or verified; it is chosen to ensure uniqueness of key-IV pairs only. This should
     * be unique with high probability as long as the generating host has a high-resolution clock OR a strong source
     * of entropy for generating private keys.
     */
    UnsignedLong nonce = UnsignedLong.ZERO;
    /**
     * This field contains the AES encrypted packed @ref memo_message
     */
    //vector<char> message;
    ByteBuffer message;

    public String get_message_data() {
        return  Base58.encode(message.array());
    }
    /// @note custom_nonce is for debugging only; do not set to a nonzero value in production
    public void set_message(private_key privateKey,
                            public_key publicKey,
                            String strMsg,
                            long lCustomNonce) {
        if (lCustomNonce == 0) {
            byte[] byteSecret = private_key.generate().get_secret();
            sha224_object sha224Object = sha224_object.create_from_byte_array(
                    byteSecret,
                    0,
                    byteSecret.length
            );

            byte[] byteEntropy = new byte[4];
            System.arraycopy(sha224Object.hash, 0, byteEntropy, 0, byteEntropy.length);

            raw_type rawType = new raw_type();
            long lEntropy = rawType.byte_array_to_int(byteEntropy);
            lEntropy <<= 32;
            lEntropy &= 0xff00000000000000l;
            nonce = UnsignedLong.fromLongBits((System.currentTimeMillis() & 0x00ffffffffffffffl) | lEntropy);
        } else {
            nonce = UnsignedLong.valueOf(lCustomNonce);
        }
        sha512_object sha512Object = privateKey.get_shared_secret(publicKey);
        String strNoncePlusSecret = nonce.toString() + sha512Object.toString();
        sha512Object = sha512_object.create_from_string(strNoncePlusSecret);

        sha256_object sha256Object = sha256_object.create_from_string(strMsg);
        byte[] byteChecksum = new byte[4];
        System.arraycopy(sha256Object.hash, 0, byteChecksum, 0, byteChecksum.length);

        raw_type rawType = new raw_type();
        int nChecksum = rawType.byte_array_to_int(byteChecksum);

        ByteBuffer byteBufferText = new memo_message(nChecksum, strMsg).serialize();
        // aes加密

        byte[] byteKey = new byte[32];
        System.arraycopy(sha512Object.hash, 0, byteKey, 0, byteKey.length);

        byte[] ivBytes = new byte[16];
        System.arraycopy(sha512Object.hash, 32, ivBytes, 0, ivBytes.length);

        message = aes.encrypt(byteKey, ivBytes, byteBufferText.array());
    }

    public String get_message(private_key privateKey, public_key publicKey) {
        sha512_object sha512Object = privateKey.get_shared_secret(publicKey);
        String strNoncePlusSecret = nonce.toString() + sha512Object.toString();
        sha512Object = sha512_object.create_from_string(strNoncePlusSecret);

        byte[] byteKey = new byte[32];
        System.arraycopy(sha512Object.hash, 0, byteKey, 0, byteKey.length);
        byte[] ivBytes = new byte[16];
        System.arraycopy(sha512Object.hash, 32, ivBytes, 0, ivBytes.length);

        ByteBuffer byteDecrypt = aes.decrypt(byteKey, ivBytes, message.array());
        memo_message memoMessage = memo_message.deserialize(byteDecrypt);

        sha256_object messageHash = sha256_object.create_from_string(memoMessage.text);
        byte[] byteChecksum = new byte[4];
        System.arraycopy(messageHash.hash, 0, byteChecksum, 0, byteChecksum.length);

        raw_type rawType = new raw_type();
        int nChecksum = rawType.byte_array_to_int(byteChecksum);
        if (nChecksum == memoMessage.checksum) {
            return memoMessage.text;
        }

        return "";
    }
}
