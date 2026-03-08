package com.investments.tracker.infrastructure.external.mbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.exception.ImportParsingException;
import com.investments.tracker.domain.model.value.Currency;

class MBankCsvRowTest {

    @Test
    void parsesStandardPlnRow() {
        String line = "18.08.2025 14:28:01;ATREM;WWA-GPW;K;3;37,20;PLN;0,44;PLN;111,60;PLN";

        MBankCsvRow row = MBankCsvRow.parse(line, 36);

        assertThat(row.instrumentName()).isEqualTo("ATREM");
        assertThat(row.exchange()).isEqualTo("WWA-GPW");
        assertThat(row.side()).isEqualTo("K");
        assertThat(row.quantity()).isEqualByComparingTo("3");
        assertThat(row.unitPrice()).isEqualByComparingTo("37.20");
        assertThat(row.priceCurrency()).isEqualTo(Currency.PLN);
        assertThat(row.commission()).isEqualByComparingTo("0.44");
        assertThat(row.commissionCurrency()).isEqualTo(Currency.PLN);
    }

    @Test
    void parsesRowWithThousandsSeparator() {
        String line = "18.08.2025 13:38:13;ATREM;WWA-GPW;K;90;37,20;PLN;13,06;PLN;3 348,00;PLN";

        MBankCsvRow row = MBankCsvRow.parse(line, 36);

        assertThat(row.quantity()).isEqualByComparingTo("90");
        assertThat(row.commission()).isEqualByComparingTo("13.06");
    }

    @Test
    void parsesEurRow() {
        String line =
                "16.09.2024 10:38:23;DTLE GR ETF;DEU-XETRA;K;709;3,4117;EUR;30,03;PLN;10 355,29;PLN";

        MBankCsvRow row = MBankCsvRow.parse(line, 36);

        assertThat(row.instrumentName()).isEqualTo("DTLE GR ETF");
        assertThat(row.exchange()).isEqualTo("DEU-XETRA");
        assertThat(row.unitPrice()).isEqualByComparingTo("3.4117");
        assertThat(row.priceCurrency()).isEqualTo(Currency.EUR);
        assertThat(row.commission()).isEqualByComparingTo("30.03");
        assertThat(row.commissionCurrency()).isEqualTo(Currency.PLN);
    }

    @Test
    void parsesSellRow() {
        String line = "18.08.2025 13:28:37;ETFBCASH;WWA-GPW;S;50;140,40;PLN;11,27;PLN;7 020,00;PLN";

        MBankCsvRow row = MBankCsvRow.parse(line, 36);

        assertThat(row.side()).isEqualTo("S");
        assertThat(row.quantity()).isEqualByComparingTo("50");
    }

    @Test
    void throwsOnWrongFieldCount() {
        String line = "too;few;fields";

        assertThatThrownBy(() -> MBankCsvRow.parse(line, 10))
                .isInstanceOf(ImportParsingException.class)
                .hasMessageContaining("expected 11 fields");
    }

    @Test
    void parsesPolishNumberWithCommaDecimal() {
        assertThat(MBankCsvRow.parsePolishNumber("37,20"))
                .isEqualByComparingTo(new BigDecimal("37.20"));
    }

    @Test
    void parsesPolishNumberWithThousandsAndDecimal() {
        assertThat(MBankCsvRow.parsePolishNumber("3 348,00"))
                .isEqualByComparingTo(new BigDecimal("3348.00"));
    }

    @Test
    void parsesPolishNumberWithFourDecimals() {
        assertThat(MBankCsvRow.parsePolishNumber("3,4117"))
                .isEqualByComparingTo(new BigDecimal("3.4117"));
    }

    @Test
    void throwsOnUnsupportedCurrency() {
        String line = "18.08.2025 14:28:01;ATREM;WWA-GPW;K;3;37,20;CHF;0,44;PLN;111,60;PLN";

        assertThatThrownBy(() -> MBankCsvRow.parse(line, 36))
                .isInstanceOf(ImportParsingException.class)
                .hasMessageContaining("unsupported currency: CHF");
    }
}
