package com.investments.tracker.domain.model.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.exception.DomainException;

@DisplayName("CatalogSyncResult value object")
class CatalogSyncResultTest {

    @Test
    @DisplayName("creates valid result with all counts")
    void createsValidResult() {
        CatalogSyncResult result = new CatalogSyncResult(100, 50, 10);

        assertThat(result.created()).isEqualTo(100);
        assertThat(result.updated()).isEqualTo(50);
        assertThat(result.skipped()).isEqualTo(10);
    }

    @Test
    @DisplayName("allows zero counts")
    void allowsZeroCounts() {
        CatalogSyncResult result = new CatalogSyncResult(0, 0, 0);

        assertThat(result.created()).isZero();
        assertThat(result.updated()).isZero();
        assertThat(result.skipped()).isZero();
    }

    @Test
    @DisplayName("throws for negative created count")
    void throwsForNegativeCreated() {
        assertThatThrownBy(() -> new CatalogSyncResult(-1, 0, 0))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("created cannot be negative");
    }

    @Test
    @DisplayName("throws for negative updated count")
    void throwsForNegativeUpdated() {
        assertThatThrownBy(() -> new CatalogSyncResult(0, -1, 0))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("updated cannot be negative");
    }

    @Test
    @DisplayName("throws for negative skipped count")
    void throwsForNegativeSkipped() {
        assertThatThrownBy(() -> new CatalogSyncResult(0, 0, -1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("skipped cannot be negative");
    }
}
