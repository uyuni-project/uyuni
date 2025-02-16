export type PeripheralsListProp = {
  peripherals: PeripheralListData[];
};

export type PeripheralListData = {
  id: string;
  fqdn: string;
  nChannelsSync: number;
  nSyncOrgs: number;
  rootCA: string;
};

export type HubDetailData = {
  id: string;
  fqdn: string;
  rootCA: string;
};

export type PeripheralDetailData = {
  id: string;
  fqdn: string;
  rootCA: string;
};
