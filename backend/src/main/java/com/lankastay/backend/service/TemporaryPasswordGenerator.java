package com.lankastay.backend.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TemporaryPasswordGenerator {
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%*-_".toCharArray();
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        char[] value = new char[20];
        for (int i = 0; i < value.length; i++) value[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        return new String(value);
    }
}
