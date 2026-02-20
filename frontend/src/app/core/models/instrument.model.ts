export interface InstrumentDTO {
  symbol: string;
  name: string;
  type: string;
  currency: string;
}

export interface InstrumentListResponse {
  instruments: InstrumentDTO[];
  totalCount: number;
}
