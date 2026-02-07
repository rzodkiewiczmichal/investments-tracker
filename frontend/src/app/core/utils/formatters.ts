/**
 * Shared formatting utilities for consistent data display across components.
 */

export type TagSeverity = 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast';

export const Formatters = {
  /**
   * Format monetary amounts in Polish locale with PLN currency.
   */
  formatMoney(amount: number): string {
    return new Intl.NumberFormat('pl-PL', {
      style: 'currency',
      currency: 'PLN',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  },

  /**
   * Format percentage values with sign indicator.
   */
  formatPercentage(value: number): string {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  },

  /**
   * Format quantity values with appropriate decimal places.
   */
  formatQuantity(quantity: number): string {
    return new Intl.NumberFormat('pl-PL', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 8
    }).format(quantity);
  },

  /**
   * Format ISO date strings in Polish locale.
   */
  formatDate(dateString: string): string {
    return new Intl.DateTimeFormat('pl-PL', {
      dateStyle: 'medium',
      timeStyle: 'short'
    }).format(new Date(dateString));
  },

  /**
   * Get CSS class for profit/loss styling.
   */
  getProfitLossClass(value: number): string {
    if (value > 0) return 'positive';
    if (value < 0) return 'negative';
    return 'neutral';
  },

  /**
   * Get PrimeNG Tag severity for instrument types.
   */
  getInstrumentTypeSeverity(type: string): TagSeverity {
    switch (type) {
      case 'STOCK': return 'info';
      case 'ETF': return 'success';
      case 'BOND': return 'warn';
      default: return 'secondary';
    }
  }
};
