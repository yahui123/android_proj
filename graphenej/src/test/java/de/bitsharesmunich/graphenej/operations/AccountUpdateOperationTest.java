package de.bitsharesmunich.graphenej.operations;

import com.google.common.primitives.UnsignedLong;

import org.bitcoinj.core.ECKey;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import de.bitsharesmunich.graphenej.AccountOptions;
import de.bitsharesmunich.graphenej.Address;
import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.AssetAmount;
import de.bitsharesmunich.graphenej.Authority;
import de.bitsharesmunich.graphenej.BaseOperation;
import de.bitsharesmunich.graphenej.BlockData;
import de.bitsharesmunich.graphenej.BrainKey;
import de.bitsharesmunich.graphenej.PublicKey;
import de.bitsharesmunich.graphenej.Transaction;
import de.bitsharesmunich.graphenej.UserAccount;
import de.bitsharesmunich.graphenej.Util;
import de.bitsharesmunich.graphenej.errors.MalformedAddressException;

/**
 * Created by nelson on 4/18/17.
 */
public class AccountUpdateOperationTest {

    private static final String BILTHON_16_BRAIN_KEY = "SOAPILY GASSING FIFIE OZONATE WHYO TOPLINE PRISMY ZEUGMA GLOTTIC DAVEN CORODY PFUI";
    public final String ADDRESS = "BDS8RYD5ehEMtTrfmeWRVKJzvLK2AqunxRh2XhXyXVxKtDjeAhYs1";

    private final Asset CORE = new Asset("1.3.0");

    private Authority active;
    private AccountOptions options;

    @Before
    public void setup(){
        try{
            HashMap<Address, Long> keyAuth = new HashMap<>();
            keyAuth.put(new Address(ADDRESS), 1l);
            active = new Authority();
            active.setKeyAuthorities(keyAuth);

            options = new AccountOptions();
            options.setMemoKey(new PublicKey(ECKey.fromPublicOnly(new Address(ADDRESS).getPublicKey().toBytes())));
            options.setNumWitness(0);
            options.setNum_comittee(0);
            options.setVotingAccount(new UserAccount("1.2.5"));
        }catch(MalformedAddressException e){
            System.out.println("MalformedAddressException. Msg: "+e.getMessage());
        }
    }

    @Test
    public void testOperationSerialization(){
        AccountUpdateOperationBuilder builder = new AccountUpdateOperationBuilder()
                .setAccount(new UserAccount("1.2.143569"))
                .setFee(new AssetAmount(UnsignedLong.valueOf(14676), CORE))
                .setActive(active)
                .setOptions(options);

        AccountUpdateOperation operation = builder.build();

        ArrayList<BaseOperation> operations = new ArrayList<>();
        operations.add(operation);
        ECKey privateKey = new BrainKey(BILTHON_16_BRAIN_KEY, 0).getPrivateKey();
        BlockData blockData = new BlockData(3703, 2015738269, 1492551764);

        Transaction tx = new Transaction(privateKey, blockData, operations);

//        String json = tx.toJsonString();
        byte[] serialized = tx.toBytes();

//        System.out.println("json: "+json);
        System.out.println("serialized: "+ Util.bytesToHex(serialized));
    }
}