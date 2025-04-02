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
  id: number;
  fqdn: string;
  rootCA: string | null;
  nSyncedChannels: number;
  nSyncedOrgs: number;
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

export enum MigrationVersion {
  v1 = "v1",
  v2 = "v2",
}

export type MigrationEntry = {
  id: number;
  selected: boolean;
  disabled: boolean;
  fqdn: string;
  accessToken: string | null;
  rootCA: string | null;
};

export enum MigrationResultCode {
  SUCCESS = "SUCCESS",
  PARTIAL = "PARTIAL",
  FAILURE = "FAILURE",
}

export enum MigrationMessageLevel {
  INFO = "INFO",
  WARN = "WARN",
  ERROR = "ERROR",
}

export type MigrationMessage = {
  severity: MigrationMessageLevel;
  message: string;
};

export type MigrationResult = {
  resultCode: MigrationResultCode;
  messageSet: Array<MigrationMessage>;
};
