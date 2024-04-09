export type ChannelAppStream = {
  channelId: number;
  channelLabel: string;
  modulesNames: Array<string>;
  modules: Map<string, Array<AppStreamModule>>;
  numberOfAppStreams: number;
};

export type AppStreamModule = {
  name: string;
  stream: string;
  version: string;
  context: string;
  arch: string;
  enabled: boolean;
};
