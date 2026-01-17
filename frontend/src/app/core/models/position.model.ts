import { Money } from './money.model';

export type InstrumentType = 'STOCK' | 'ETF' | 'BOND_ETF' | 'POLISH_GOV_BOND';

export interface PositionSummary {
  id: string;
  instrumentName: string;
  instrumentSymbol: string;
  instrumentType: InstrumentType;
  quantity: number;
  averageCost: Money;
  currentValue: Money;
  investedAmount: Money;
  profitLoss: Money;
  returnPercentage: number;
}

export interface PositionDetail extends PositionSummary {
  accountId: string;
  accountName: string;
  currentPrice: Money;
  createdAt: string;
  updatedAt: string;
}

export interface PositionsResponse {
  positions: PositionSummary[];
  totalCount: number;
}

export interface AddPositionCommand {
  instrumentName: string;
  instrumentSymbol: string;
  instrumentType: InstrumentType;
  accountId: string;
  quantity: number;
  averageCost: number;
}
