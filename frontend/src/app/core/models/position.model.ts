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
  currentValue: Money;
  investedAmount: Money;
  profitLoss: Money;
  returnPercentage: number;
}

export interface PositionDetail extends PositionSummary {
  currentPrice: Money;
  holdings: AccountHoldingDTO[];
}

export interface PositionsResponse {
  positions: PositionSummary[];
  totalCount: number;
}

export interface AddPositionCommand {
  instrumentName: string;
  instrumentSymbol: string;
  instrumentType: InstrumentType;
  accountId: number;
  quantity: number;
  averageCost: number;
}
