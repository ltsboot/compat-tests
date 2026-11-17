package io.github.ltsboot.lts.compat.cache;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically drops cached prices so they are reloaded from the backend. */
@Component
public class PriceRefreshJob {

    private final CacheManager cacheManager;
    private final AtomicInteger runs = new AtomicInteger();

    public PriceRefreshJob(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(initialDelayString = "${app.refresh.initial-delay-ms:60000}",
            fixedDelayString = "${app.refresh.delay-ms:60000}")
    public void refreshPrices() {
        Cache prices = cacheManager.getCache("prices");
        if (prices != null) {
            prices.clear();
        }
        runs.incrementAndGet();
    }

    public int runs() {
        return runs.get();
    }
}
