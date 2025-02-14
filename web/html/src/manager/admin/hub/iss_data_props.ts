export type PeripheralsListProp = {
  peripherals: PeripheralListData[];
};

export type PeripheralListData = {
  id: string;
  fqdn: string;
  nChannelsSync: number;
  nAllChannels: number;
  nOrgs: number;
};

export type HubDetailData = {
  id: string;
  fqdn: string;
};

export type PeripheralDetailData = {
  id: string;
  fqdn: string;
};

