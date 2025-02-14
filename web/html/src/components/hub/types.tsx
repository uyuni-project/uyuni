export type HubRegisterRequest = {
  fqdn: string;
  token?: string;
  username?: string;
  password?: string;
  rootCA?: string;
};
