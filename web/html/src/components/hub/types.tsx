export enum TokenType {
  ISSUED = "ISSUED",
  CONSUMED = "CONSUMED",
}

export type AccessToken = {
  id: number;
  serverFqdn: string;
  type: TokenType;
  localizedType: string;
  valid: boolean;
  expirationDate: Date | null;
  creationDate: Date;
  modificationDate: Date;
  hubId: number | null;
  peripheralId: number | null;
};

export enum IssRole {
  Hub = "HUB",
  Peripheral = "PERIPHERAL",
}

export type HubRegisterRequest = {
  fqdn: string;
  token?: string;
  username?: string;
  password?: string;
  rootCA?: string;
};

export type ValidityRequest = {
  valid: boolean;
};

export type CreateTokenRequest = {
  type: TokenType;
  fqdn?: string;
  token?: string;
};

export type PeripheralListData = {
  id: string;
  fqdn: string;
  nChannelsSync: number;
  nAllChannels: number;
  nOrgs: number;
};

export type HubDetailData = {
  id: number;
  fqdn: string;
  rootCA: string | null;
  gpgKey: string | null;
  sccUsername: string;
  created: Date;
  modified: Date;
};
