export interface ImportSessionResponse {
  importSessionId: string;
  status: 'PENDING_REVIEW' | 'READY_TO_CONFIRM' | 'PENDING_PRICES' | 'COMPLETED';
  broker: string;
  accountName: string;
  summary: ImportSummary;
  createdAt: string;
  completedAt: string | null;
}

export interface ImportSummary {
  totalTransactions: number;
  matchedInstruments: number;
  unmatchedInstruments: number;
  unmatchedDetails: UnmatchedInstrument[];
  instrumentsNeedingPrices: InstrumentNeedingPrice[];
}

export interface UnmatchedInstrument {
  brokerName: string;
  suggestions: InstrumentSuggestion[];
}

export interface InstrumentSuggestion {
  symbol: string;
  name: string;
}

export interface InstrumentNeedingPrice {
  symbol: string;
  name: string;
  currency: string;
}

export interface MappingEntry {
  brokerName: string;
  catalogSymbol: string;
}

export interface PriceEntry {
  symbol: string;
  price: string;
  currency: string;
}
