export enum TokenType {
  ISSUED = "ISSUED",
  CONSUMED = "CONSUMED",
}

export interface AccessToken {
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
}

export enum IssRole {
  Hub = "HUB",
  Peripheral = "PERIPHERAL",
}

export interface HubRegisterRequest {
  fqdn: string;
  token?: string;
  username?: string;
  password?: string;
  rootCA?: string;
}

export interface ValidityRequest {
  valid: boolean;
}

export interface CreateTokenRequest {
  type: TokenType;
  fqdn?: string;
  token?: string;
}

export interface PeripheralListData {
  id: number;
  fqdn: string;
  rootCA: string | null;
  nSyncedChannels: number;
  nSyncedOrgs: number;
}

export interface IssServerDetailData {
  id: number;
  role: IssRole;
  fqdn: string;
  rootCA: string | null;
  sccUsername: string;
  created: Date;
  modified: Date;
}

export interface HubDetailData extends IssServerDetailData {
  gpgKey: string | null;
}

export interface PeripheralDetailData extends IssServerDetailData {
  nSyncedChannels: number;
  nSyncedOrgs: number;
}

export enum MigrationVersion {
  v1 = "v1",
  v2 = "v2",
}

export interface MigrationEntry {
  id: number;
  selected: boolean;
  disabled: boolean;
  fqdn: string;
  accessToken: string | null;
  rootCA: string | null;
}

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

export interface MigrationMessage {
  severity: MigrationMessageLevel;
  message: string;
}

export interface MigrationResult {
  resultCode: MigrationResultCode;
  messageSet: MigrationMessage[];
}

export interface Org {
  orgId: number;
  orgName: string;
}

export interface Channel {
  channelId: number;
  channelName: string;
  channelLabel: string;
  channelArch: string;
  channelOrg: Org | null;
  selectedPeripheralOrg: Org | null;
  parentChannelLabel: string | null; // if null, this is a root channel
  children: Channel[]; // for easy hierarchical references
  strictOrg: boolean;
  synced: boolean;
}

export interface FlatChannel {
  channelId: number;
  channelName: string;
  channelLabel: string;
  channelArch: string;
  channelOrg: Org | null;
  selectedPeripheralOrg: Org | null;
  parentChannelLabel: string | null; // if null, this is a root channel
  childrenLabels: string[]; // for easy lookup if needed
  strictOrg: boolean;
  synced: boolean; // no need for another class that tells us if the channel is synced or not
}

export interface ChannelSyncProps {
  peripheralOrgs: Org[];
  channels: Channel[];
}
