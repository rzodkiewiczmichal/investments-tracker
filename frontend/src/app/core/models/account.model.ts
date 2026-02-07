export interface Account {
  id: number;
  name: string;
  broker: string;
  createdAt?: string;
}

export interface AccountsResponse {
  accounts: Account[];
  totalCount: number;
}
