import { localizedMoment } from "utils";

import { AccessToken, IssRole, MigrationEntry, PeripheralDetailData, PeripheralListData, TokenType } from "./types";

export const peripheralDetails: PeripheralDetailData = {
  id: 42,
  role: IssRole.Peripheral,
  fqdn: "peripheral.example.com",
  rootCA: ["-----BEGIN CERTIFICATE-----", "MIIBstorybookcertificate", "-----END CERTIFICATE-----"].join("\n"),
  sccUsername: "mirror-peripheral",
  created: localizedMoment("2026-08-15T09:00:00Z").toDate(),
  modified: localizedMoment("2026-09-22T14:00:00Z").toDate(),
  nSyncedChannels: 12,
  nSyncedOrgs: 2,
};

export const createAccessTokens = (): AccessToken[] => [
  {
    id: 1,
    serverFqdn: "hub-europe.example.com",
    type: TokenType.ISSUED,
    localizedType: "Issued",
    valid: true,
    expirationDate: null,
    creationDate: localizedMoment("2026-08-12T10:00:00Z").toDate(),
    modificationDate: localizedMoment("2026-09-18T15:30:00Z").toDate(),
    hubId: 17,
    peripheralId: null,
  },
  {
    id: 2,
    serverFqdn: "peripheral-lab.example.com",
    type: TokenType.CONSUMED,
    localizedType: "Consumed",
    valid: true,
    expirationDate: localizedMoment("2027-01-31T23:59:59Z").toDate(),
    creationDate: localizedMoment("2026-07-04T08:15:00Z").toDate(),
    modificationDate: localizedMoment("2026-09-02T11:45:00Z").toDate(),
    hubId: null,
    peripheralId: 42,
  },
  {
    id: 3,
    serverFqdn: "retired.example.com",
    type: TokenType.ISSUED,
    localizedType: "Issued",
    valid: false,
    expirationDate: localizedMoment("2026-06-30T23:59:59Z").toDate(),
    creationDate: localizedMoment("2026-05-20T12:00:00Z").toDate(),
    modificationDate: localizedMoment("2026-07-01T07:00:00Z").toDate(),
    hubId: null,
    peripheralId: null,
  },
];

export const migrationEntries: MigrationEntry[] = [
  {
    id: 1,
    fqdn: "legacy-east.example.com",
    accessToken: "eyJhbGciOiJIUzI1NiJ9.east",
    rootCA: null,
    disabled: false,
    selected: true,
  },
  {
    id: 2,
    fqdn: "legacy-west.example.com",
    accessToken: null,
    rootCA: ["-----BEGIN CERTIFICATE-----", "MIIBstorybookwestcertificate", "-----END CERTIFICATE-----"].join("\n"),
    disabled: false,
    selected: false,
  },
  {
    id: 3,
    fqdn: "legacy-disabled.example.com",
    accessToken: "eyJhbGciOiJIUzI1NiJ9.disabled",
    rootCA: null,
    disabled: true,
    selected: false,
  },
];

export const peripherals: PeripheralListData[] = [
  {
    id: 42,
    fqdn: "peripheral-europe.example.com",
    rootCA: ["-----BEGIN CERTIFICATE-----", "MIIBstorybookeuropecertificate", "-----END CERTIFICATE-----"].join("\n"),
    nSyncedChannels: 18,
    nSyncedOrgs: 3,
  },
  {
    id: 43,
    fqdn: "peripheral-lab.example.com",
    rootCA: null,
    nSyncedChannels: 4,
    nSyncedOrgs: 1,
  },
  {
    id: 44,
    fqdn: "peripheral-empty.example.com",
    rootCA: null,
    nSyncedChannels: 0,
    nSyncedOrgs: 0,
  },
];
