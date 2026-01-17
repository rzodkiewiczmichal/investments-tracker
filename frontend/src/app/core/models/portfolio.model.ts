import { Money } from './money.model';

export interface PortfolioSummary {
  totalCurrentValue: Money;
  totalInvestedAmount: Money;
  totalProfitLoss: Money;
  totalReturnPercentage: number;
  positionsCount: number;
  lastUpdatedAt: string;
}
