export interface Channel {
  id: number;
  label: string;
  name: string;
}

export type AppStreams = Record<string, AppStreamModule[]>;

export interface ChannelAppStream {
  channel: Channel;
  appStreams: AppStreams;
}

export interface AppStreamModule {
  name: string;
  stream: string;
  version: string;
  context: string;
  arch: string;
  enabled: boolean;
}
