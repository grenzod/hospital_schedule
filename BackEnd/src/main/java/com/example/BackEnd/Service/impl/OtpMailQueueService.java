package com.example.BackEnd.Service.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.example.BackEnd.Model.OtpMail;

@Service
@Slf4j
public class OtpMailQueueService {
    private final BlockingQueue<OtpMail> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executor;
    private final VerificationService verificationService;

    public OtpMailQueueService(VerificationService verificationService) {
        this.verificationService = verificationService;
        this.executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executor.submit(this::processQueue);
        }
    }

    public void enqueue(OtpMail mail) {
        if (!queue.offer(mail)) {
            log.warn("Queue full, failed to enqueue mail for {}", mail.getTo());
        }
    }

    private void processQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                OtpMail mail = queue.take();
                verificationService.sendOtpEmailSync(mail.getTo(), mail.getOtp());
                log.info("Sent OTP mail to {}", mail.getTo());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error("Failed to send OTP mail", ex);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
