package io.github.ltsboot.lts.compat.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.refresh.initial-delay-ms=3600000")
class CatalogServiceTests {

    @Autowired
    private CatalogService catalog;

    @Autowired
    private PriceRefreshJob refreshJob;

    @Test
    void repeatedLookupIsServedFromCache() {
        int before = catalog.backendCalls();

        assertThat(catalog.priceInCents("CACHE-A")).isEqualTo(700L);
        assertThat(catalog.priceInCents("CACHE-A")).isEqualTo(700L);

        assertThat(catalog.backendCalls() - before).isEqualTo(1);
    }

    @Test
    void scheduledRefreshEvictsCachedPrices() {
        catalog.priceInCents("EVICT-B");
        int afterFirstLookup = catalog.backendCalls();

        refreshJob.refreshPrices();
        catalog.priceInCents("EVICT-B");

        assertThat(catalog.backendCalls() - afterFirstLookup).isEqualTo(1);
    }

    @Test
    void blankSkuIsRejectedByMethodValidation() {
        assertThatThrownBy(() -> catalog.priceInCents("   "))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void nonPositiveQuantityIsRejectedByMethodValidation() {
        assertThatThrownBy(() -> catalog.totalCents("QTY-C", 0))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("quantity");
    }
}
