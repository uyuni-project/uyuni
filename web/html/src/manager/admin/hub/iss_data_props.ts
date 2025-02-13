export type HubsList = {
  hubs: HubData[];
};

export type PeripheralsList = {
  peripherals: PeripheralData[];
};

export type HubData = {
  fqdn: String;
  defaultHub: boolean;
  knownOrgs: number;
  unmappedOrgs: number;
};

export type PeripheralData = {
  fqdn: string;
  allowSync: boolean;
  allowAllOrgs: boolean;
  nOfOrgsExported: String;
};
