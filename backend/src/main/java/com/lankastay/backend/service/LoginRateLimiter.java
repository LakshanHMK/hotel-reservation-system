package com.lankastay.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final int maximum;
    private final Duration window;

    public LoginRateLimiter(@Value("${lankastay.security.ip-max-attempts:20}") int maximum,
                            @Value("${lankastay.security.ip-window:15m}") Duration window) {
        this.maximum = maximum;
        this.window = window;
    }

    public synchronized boolean allow(String ip) {
        Instant cutoff = Instant.now().minus(window);
        Deque<Instant> history = attempts.computeIfAbsent(ip == null ? "unknown" : ip, ignored -> new ArrayDeque<>());
        while (!history.isEmpty() && history.peekFirst().isBefore(cutoff)) history.removeFirst();
        if (history.size() >= maximum) return false;
        history.addLast(Instant.now());
        return true;
    }

    public synchronized void clear(String ip) { attempts.remove(ip == null ? "unknown" : ip); }
}
