package io.github.ltsboot.lts.compat.cache;

import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** Method-validated service whose price lookups are cached in the "prices" cache. */
@Service
@Validated
public class CatalogService {

    private final AtomicInteger backendCalls = new AtomicInteger();

    @Cacheable(cacheNames = "prices", key = "#sku")
    public long priceInCents(@NotBlank @Size(max = 20) String sku) {
        backendCalls.incrementAndGet(); // stands in for a slow remote call
        return 100L * sku.length();
    }

    public long totalCents(@NotBlank @Size(max = 20) String sku, @Min(1) int quantity) {
        return priceInCents(sku) * quantity;
    }

    public int backendCalls() {
        return backendCalls.get();
    }
}
