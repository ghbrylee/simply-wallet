package com.brylee.service.wallet;

import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@RequiredArgsConstructor
public class WalletConfig {

    @Value("${bitcoin.network}")
    private String network;

    @Value("${bitcoin.file-prefix}")
    private String filePrefix;

    @Value("${bitcoin.file-location}")
    private String btcFileLocation;

    public NetworkParameters networkParameters() {
        if(network.equals("testnet")) {
            return TestNet3Params.get();
        } else if(network.equals("regtest")) {
            return RegTestParams.get();
        } else {
            return MainNetParams.get();
        }
    }

    public WalletAppKit walletAppKit(NetworkParameters networkParameters) {
        return new WalletAppKit(networkParameters, new File(btcFileLocation), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                if (wallet().getKeyChainGroupSize() < 1) {
                    wallet().importKey(new ECKey());
                }
            }
        };
    }

}