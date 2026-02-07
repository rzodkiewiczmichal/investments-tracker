export interface Account {
  id: number;
  name: string;
  broker: string;

}

export interface AccountsResponse {
  accounts: Account[];
  totalCount: number;
}

export interface CreateAccountRequest {
  name: string;
  broker: string;
}
