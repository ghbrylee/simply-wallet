package com.brylee.service.wallet;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WalletService {
    private final WalletConfig walletConfig;
    private NetworkParameters networkParameters;
    private WalletAppKit walletAppKit;

    public void load() {
        //  0.0057248 BTC

        networkParameters = walletConfig.networkParameters();
        walletAppKit = walletConfig.walletAppKit(networkParameters);


        walletAppKit.startAsync();
        walletAppKit.awaitRunning();

        walletAppKit.wallet().addCoinsReceivedEventListener(
                (wallet, tx, prevBalance, newBalance) -> {
                    Coin value = tx.getValueSentToMe(wallet);
                    System.out.println("Received tx for " + value.toFriendlyString());
                    Futures.addCallback(tx.getConfidence().getDepthFuture(1),
                            new FutureCallback<TransactionConfidence>() {
                                @Override
                                public void onSuccess(TransactionConfidence result) {
                                    System.out.println("Received tx " +
                                            value.toFriendlyString() + " is confirmed. ");
                                }

                                @Override
                                public void onFailure(Throwable t) {}
                            }, MoreExecutors.directExecutor());
                });

        Address sendToAddress = LegacyAddress.fromKey(networkParameters, walletAppKit.wallet().currentReceiveKey());
        System.out.println("Send coins to: " + sendToAddress);
    }

    public void send(String valueStr, String toStr) {
        Coin value = Coin.parseCoin(valueStr);

        LegacyAddress to = LegacyAddress.fromBase58(networkParameters, toStr);
        System.out.println("Send money to: " + to.toString());

        try {
            Address toAddress = LegacyAddress.fromBase58(networkParameters, toStr);
            SendRequest sendRequest = SendRequest.to(toAddress, Coin.parseCoin(valueStr));
            sendRequest.feePerKb = Coin.parseCoin("0.0002");
            Wallet.SendResult sendResult = walletAppKit.wallet().sendCoins(walletAppKit.peerGroup(), sendRequest);
            sendResult.broadcastComplete.addListener(() ->
                            System.out.println("Sent coins onwards! Transaction hash is " + sendResult.tx.getTxId()),
                    MoreExecutors.directExecutor());
        } catch (InsufficientMoneyException e) {
            throw new RuntimeException(e);
        }
    }

    public void createMultisigAddress() throws UnreadableWalletException, InsufficientMoneyException {
        String seedCode = "yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        String passphrase = "";
        Long creationtime = 1409478661L;
        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, creationtime);

        // The wallet class provides a easy fromSeed() function that loads a new wallet from a given seed.
//        Wallet wallet = Wallet.fromSeed(networkParameters, seed, Script.ScriptType.P2PKH);
        Wallet walletA = Wallet.createBasic(networkParameters);
        Wallet walletB = Wallet.createBasic(networkParameters);
        Wallet walletC = Wallet.createBasic(networkParameters);

        ECKey keyA = walletA.currentReceiveKey(); // 랜덤키 생성
        ECKey keyB = walletB.currentReceiveKey();
        ECKey keyC = walletC.currentReceiveKey();

        Transaction aTransaction = new Transaction(networkParameters);
        List<ECKey> keyList = ImmutableList.of(keyA, keyB, keyC);


        Script script = ScriptBuilder.createMultiSigOutputScript(2, keyList); //2 of 3 multisig
        Coin value = Coin.valueOf(0,10); // 0.1 btc
        aTransaction.addOutput(value, script);

        SendRequest request = SendRequest.forTx(aTransaction);

        KeyChainGroup keyChainGroup = null;
        Wallet wallet = new Wallet(networkParameters, keyChainGroup);
        wallet.completeTx(request); // fill in coins

    }
}
