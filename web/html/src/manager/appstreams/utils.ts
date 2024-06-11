import { AppStreamModule, Channel, ChannelAppStream } from "./appstreams.type";

export const getStreamName = (module: AppStreamModule) => `${module.name}:${module.stream}`;

export const handleModuleEnableDisable = (
  channel: Channel,
  appStream: AppStreamModule,
  appStreams: ChannelAppStream[],
  toEnable: Map<number, string[]>,
  toDisable: Map<number, string[]>,
  setToEnable: React.Dispatch<React.SetStateAction<Map<number, string[]>>>,
  setToDisable: React.Dispatch<React.SetStateAction<Map<number, string[]>>>
) => {
  const handleReactStateChange = (prevState: Map<number, string[]>, stream: string) => {
    const updatedMap = new Map<number, string[]>(prevState);
    let newList = updatedMap.get(channel.id) ?? [];
    newList = newList.includes(stream) ? newList.filter((it) => it !== stream) : newList.concat(stream);
    updatedMap.set(channel.id, newList);
    return updatedMap;
  };

  const isStreamEnabled = (stream: AppStreamModule) =>
    toEnable.get(channel.id)?.includes(getStreamName(stream)) ||
    (stream.enabled && !toDisable.get(channel.id)?.includes(getStreamName(stream)));

  const stream = getStreamName(appStream);
  const setStateFunction = appStream.enabled ? setToDisable : setToEnable;
  setStateFunction((prevState) => handleReactStateChange(prevState, stream));

  // Disable every other stream of the module
  appStreams.forEach((ch) =>
    ch.appStreams[appStream.name]
      ?.filter((as) => getStreamName(as) !== stream && isStreamEnabled(as))
      .forEach((as) =>
        handleModuleEnableDisable(ch.channel, as, appStreams, toEnable, toDisable, setToEnable, setToDisable)
      )
  );
};

export const numberOfChanges = (toEnable: Map<number, string[]>, toDisable: Map<number, string[]>) => {
  return reduceMapListLength(toEnable) + reduceMapListLength(toDisable);
};

const reduceMapListLength = (mapToReduce: Map<number, string[]>) => {
  return Array.from(mapToReduce.keys()).reduce((acc, key) => acc + (mapToReduce.get(key)?.length ?? 0), 0);
};
