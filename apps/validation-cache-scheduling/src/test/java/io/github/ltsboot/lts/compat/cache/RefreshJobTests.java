package io.github.ltsboot.lts.compat.cache;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"app.refresh.initial-delay-ms=0", "app.refresh.delay-ms=50"})
class RefreshJobTests {

    @Autowired
    private PriceRefreshJob refreshJob;

    @Test
    void scheduledJobRunsRepeatedly() {
        await().atMost(Duration.ofSeconds(10))
                .until(() -> refreshJob.runs() >= 3);

        assertThat(refreshJob.runs()).isGreaterThanOrEqualTo(3);
    }
}
