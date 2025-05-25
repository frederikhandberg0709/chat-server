package com.frederikhandberg.service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KeyStoreManager {

    @Value("${jwt.keystore.path}")
    private String keystorePath;

    @Value("${jwt.keystore.password}")
    private String keystorePassword;

    @Value("${jwt.key.alias}")
    private String keyAlias;

    private final ResourceLoader resourceLoader;
    private KeyPair currentKeyPair;

    public KeyStoreManager(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadKeys() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            Resource resource = resourceLoader.getResource(keystorePath);

            if (!resource.exists()) {
                throw new FileNotFoundException("Keystore file not found at: " + keystorePath);
            }

            try (InputStream is = resource.getInputStream()) {
                keyStore.load(is, keystorePassword.toCharArray());

                Key key = keyStore.getKey(keyAlias, keystorePassword.toCharArray());
                if (!(key instanceof PrivateKey)) {
                    throw new SecurityException("Unexpected key type");
                }
                PrivateKey privateKey = (PrivateKey) key;

                Certificate cert = keyStore.getCertificate(keyAlias);
                PublicKey publicKey = cert.getPublicKey();

                this.currentKeyPair = new KeyPair(publicKey, privateKey);

                log.info("Successfully loaded keys from KeyStore");
            }
        } catch (Exception e) {
            log.error("Failed to load keys from KeyStore", e);
            throw new SecurityException("Failed to initialize KeyStore", e);
        }
    }

    public KeyPair getCurrentKeyPair() {
        if (currentKeyPair == null) {
            throw new SecurityException("Keys not initialized");
        }
        return currentKeyPair;
    }
}
