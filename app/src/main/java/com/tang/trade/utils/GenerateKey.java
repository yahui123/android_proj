package com.tang.trade.utils;

import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.BrainKey;

/**
 * 生成公钥私钥
 * Created by Administrator on 2018/4/12.
 */

public class GenerateKey {

    private static String suggestPublicKey;
    private static String suggestPrivateKey;

    public static String getSuggestPublicKey() {
        return suggestPublicKey;
    }

    public static String getSuggestPrivateKey() {
        return suggestPrivateKey;
    }


    private static void GenerateKey(String word){
            BrainKey mBrainKey = new BrainKey(word, BrainKey.DEFAULT_SEQUENCE_NUMBER);
                    Address address = mBrainKey.getPublicAddress(Address.BITSHARES_PREFIX);
                     suggestPublicKey = address.toString();
                      suggestPrivateKey = mBrainKey.getWalletImportFormat();
    }

    /**
     * 生成私钥
     *
     * @param word
     */
    public static void GenerateKeyOption(final String word, final KeyListener listener) {
        RxUtils.RxThread(new RxUtils.RxListener<String>() {
            @Override
            public String Option() {
                GenerateKey(word);
                return "";
            }

            @Override
            public void onUI(String data) {
                listener.backKey(GenerateKey.getSuggestPublicKey(), GenerateKey.getSuggestPrivateKey());
            }
        });
    }


    public interface KeyListener {
        void backKey(String publicKey, String private_key);

    }

}
