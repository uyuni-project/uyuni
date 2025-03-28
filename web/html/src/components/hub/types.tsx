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

export type Org = {
  orgId: number;
  orgName: string;
};

export type Channel = {
  channelId: number;
  channelName: string;
  channelLabel: string;
  channelArch: string;
  channelOrg: Org | null;
  parentChannelLabel: string | null; // if null or undefined, this is a root channel
  children: Channel[]; // for easy hierarchical references
  originalChannelLabel: string | null; // the id of the channel that this is a clone of
  synced: boolean;
};

export type FlatChannel = {
  channelId: number;
  channelName: string;
  channelLabel: string;
  channelArch: string;
  channelOrg: Org | null;
  parentChannelLabel: string | null; // if null or undefined, this is a root channel
  originalChannelLabel: string | null; // the id of the channel that this is a clone of
  childrenIds: string[];
};
