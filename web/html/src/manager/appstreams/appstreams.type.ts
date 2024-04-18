export type Channel = {
  id: number;
  label: string;
  name: string;
};

export type ChannelAppStream = {
  channel: Channel;
  appStreams: Map<string, Array<AppStreamModule>>;
};

export type AppStreamModule = {
  name: string;
  stream: string;
  version: string;
  context: string;
  arch: string;
  enabled: boolean;
};
