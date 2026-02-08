import { Money } from './money.model';

export interface PortfolioSummary {
  totalCurrentValue: Money | null;
  totalInvestedAmount: Money;
  totalProfitLoss: Money | null;
  totalReturnPercentage: number | null;
  positionsCount: number;
  message?: string;
}
