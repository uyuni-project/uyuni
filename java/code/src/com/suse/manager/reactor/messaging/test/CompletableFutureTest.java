package com.suse.manager.reactor.messaging.test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by matei on 11/7/16.
 */
public class CompletableFutureTest {

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        });
        f.handle((r, e) -> {
            if (e instanceof CancellationException) {
                System.out.println("canceled");
            } else {
                System.out.println("result " + r);
                return r;
            }
            return r;
        });
        System.out.println("waiting...");
        try {
            int r = f.get(2, TimeUnit.SECONDS);
            System.out.println("r=" + r);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("timed out");
            e.printStackTrace();
        }
        Thread.sleep(5000L);
        f.cancel(true);

        Thread.sleep(6000L);
    }
}
