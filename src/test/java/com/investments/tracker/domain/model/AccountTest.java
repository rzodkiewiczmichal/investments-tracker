package com.investments.tracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.investments.tracker.domain.exception.DomainException;
import com.investments.tracker.domain.model.value.AccountId;
import com.investments.tracker.domain.model.value.AccountName;
import com.investments.tracker.domain.model.value.BrokerName;

@DisplayName("Account aggregate")
class AccountTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates Account with valid id, name and broker")
        void createsWithValidIdNameAndBroker() {
            AccountId id = new AccountId(1L);
            AccountName name = AccountName.of("My IKE");
            BrokerName broker = BrokerName.of("XTB");

            Account account = new Account(id, name, broker);

            assertThat(account.id()).isEqualTo(id);
            assertThat(account.name().value()).isEqualTo("My IKE");
            assertThat(account.brokerName().value()).isEqualTo("XTB");
        }

        @Test
        @DisplayName("throws exception for null id")
        void throwsForNullId() {
            assertThatThrownBy(
                            () -> new Account(null, AccountName.of("My IKE"), BrokerName.of("XTB")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Account ID cannot be null");
        }

        @Test
        @DisplayName("throws exception for null name")
        void throwsForNullName() {
            assertThatThrownBy(() -> new Account(new AccountId(1L), null, BrokerName.of("XTB")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Account name cannot be null");
        }

        @Test
        @DisplayName("throws exception for null broker name")
        void throwsForNullBrokerName() {
            assertThatThrownBy(() -> new Account(new AccountId(1L), AccountName.of("My IKE"), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Broker name cannot be null");
        }
    }

    @Nested
    @DisplayName("value type validation")
    class ValueTypeValidation {

        @Test
        @DisplayName("AccountName trims whitespace")
        void accountNameTrimsWhitespace() {
            AccountName name = AccountName.of("  My IKE  ");
            assertThat(name.value()).isEqualTo("My IKE");
        }

        @Test
        @DisplayName("BrokerName trims whitespace")
        void brokerNameTrimsWhitespace() {
            BrokerName broker = BrokerName.of("  XTB  ");
            assertThat(broker.value()).isEqualTo("XTB");
        }

        @Test
        @DisplayName("AccountName throws for null value")
        void accountNameThrowsForNull() {
            assertThatThrownBy(() -> AccountName.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Account name cannot be null");
        }

        @Test
        @DisplayName("AccountName throws for blank value")
        void accountNameThrowsForBlank() {
            assertThatThrownBy(() -> AccountName.of("   "))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Account name cannot be blank");
        }

        @Test
        @DisplayName("BrokerName throws for null value")
        void brokerNameThrowsForNull() {
            assertThatThrownBy(() -> BrokerName.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Broker name cannot be null");
        }

        @Test
        @DisplayName("BrokerName throws for blank value")
        void brokerNameThrowsForBlank() {
            assertThatThrownBy(() -> BrokerName.of("   "))
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Broker name cannot be blank");
        }
    }

    @Nested
    @DisplayName("equality (identity-based)")
    class Equality {

        @Test
        @DisplayName("equals by ID only (identity)")
        void equalsByIdOnly() {
            AccountId id = new AccountId(1L);

            Account a1 = new Account(id, AccountName.of("Account 1"), BrokerName.of("XTB"));
            Account a2 = new Account(id, AccountName.of("Account 1"), BrokerName.of("XTB"));

            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("equals same ID with different state (DDD entity semantics)")
        void equalsSameIdDifferentState() {
            AccountId id = new AccountId(1L);

            Account a1 = new Account(id, AccountName.of("Account 1"), BrokerName.of("XTB"));
            Account a2 = new Account(id, AccountName.of("Account 2"), BrokerName.of("mBank"));

            // Same ID = same entity, regardless of different name/broker
            assertThat(a1).isEqualTo(a2);
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }

        @Test
        @DisplayName("not equals different IDs")
        void notEqualsDifferentIds() {
            Account a1 =
                    new Account(new AccountId(1L), AccountName.of("Account"), BrokerName.of("XTB"));
            Account a2 =
                    new Account(new AccountId(2L), AccountName.of("Account"), BrokerName.of("XTB"));

            assertThat(a1).isNotEqualTo(a2);
        }
    }
}
