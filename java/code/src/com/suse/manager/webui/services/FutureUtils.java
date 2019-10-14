package com.suse.manager.webui.services;

import java.util.concurrent.*;

public class FutureUtils {

    private static final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(1);

    public static <T> CompletableFuture<T> failAfter(int seconds) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        scheduledExecutorService.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + seconds);
            return promise.completeExceptionally(ex);
        }, seconds, TimeUnit.SECONDS);

        return promise;
    }
}
