export type Channel = {
  id: number;
  label: string;
  name: string;
};

export type AppStreams = {
  [key: string]: Array<AppStreamModule>;
};

export type ChannelAppStream = {
  channel: Channel;
  appStreams: AppStreams;
};

export type AppStreamModule = {
  name: string;
  stream: string;
  version: string;
  context: string;
  arch: string;
  enabled: boolean;
};
