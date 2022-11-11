package com.brylee.controller;

import com.brylee.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/load")
    public void load() throws Exception {
        walletService.load();
    }

    @PostMapping("/send")
    public void send(@RequestParam String amount, @RequestParam String address) throws Exception{
        walletService.send(amount, address);
    }

    @PostMapping("/create")
    public void send() throws Exception{
        walletService.createMultisigAddress();
    }

}
