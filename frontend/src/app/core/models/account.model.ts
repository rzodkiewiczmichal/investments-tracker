export type AccountType = 'NORMAL' | 'IKE' | 'IKZE';

export interface Account {
  id: string;
  name: string;
  broker: string;
  accountType: AccountType;
  createdAt: string;
}

export interface AccountsResponse {
  accounts: Account[];
  totalCount: number;
}
