package me.kubbidev.verdure;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;

@Slf4j
public class VerdureClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        log.info("Hello World (Client)");
    }
}
