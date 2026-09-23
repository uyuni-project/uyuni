import { localizedMoment } from "utils";

import { AccessToken, IssRole, PeripheralDetailData, TokenType } from "./types";

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
