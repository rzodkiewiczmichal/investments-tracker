import { Money } from './money.model';

export type InstrumentType = 'STOCK' | 'ETF' | 'BOND';

export interface AccountHoldingDTO {
  accountId: number;
  accountName: string;
  quantity: number;
  costBasis: Money;
}

export interface PositionSummary {
  instrumentSymbol: string;
  instrumentName: string;
  instrumentType: InstrumentType;
  quantity: number;
  averageCost: Money;
  currentValue: Money | null;
  investedAmount: Money;
  profitLoss: Money | null;
  returnPercentage: number | null;
}

export interface PositionDetail extends PositionSummary {
  currentPrice: Money | null;
  holdings: AccountHoldingDTO[];
}

export interface PositionsResponse {
  positions: PositionSummary[];
  totalCount: number;
}

export interface AddPositionCommand {
  instrumentSymbol: string;
  accountId: number;
  quantity: number;
  averageCost: number;
}
