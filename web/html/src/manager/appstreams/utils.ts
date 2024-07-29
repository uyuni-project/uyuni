import { AppStreamModule, Channel, ChannelAppStream } from "./appstreams.type";

export const getStreamName = (module: AppStreamModule) => `${module.name}:${module.stream}`;

/**
 * Handle the event of a click to enable or to disable a given @param appStream in the context
 * of a @param channel. For doing its job, the function needs to know the current state
 * (@param toEnable/@param toDisable) and to have proper functions to call for changing
 * the state (@param setToEnable/@param setToDisable);
 */
export const handleModuleEnableDisable = (
  channel: Channel,
  appStream: AppStreamModule,
  appStreams: ChannelAppStream[],
  toEnable: Map<number, string[]>,
  toDisable: Map<number, string[]>,
  setToEnable: React.Dispatch<React.SetStateAction<Map<number, string[]>>>,
  setToDisable: React.Dispatch<React.SetStateAction<Map<number, string[]>>>
) => {
  const stream = getStreamName(appStream);

  /**
   * We are handling an event to enable or disable a given appStream. The appStream.enabled
   * property is managed in backend side, so it indicates the status before any changes in React.
   *
   * If the appStream is enabled it only makes sense to include or remove it in the toDisable list,
   * therefore, the correct React state change function to call is setToDisable.
   *
   * Conversely, if the appStream is disabled in the backend, it only makes sense to include or
   * remove it in the toEnable list.
   * Hence, the correct React state change function to call is setToEnable.
   */
  const setStateFunction = appStream.enabled ? setToDisable : setToEnable;

  // After defining the correct React set state function to call, we invoke it.
  // The handleReactStateChange function is passed as a callback to construct the updated Map.
  setStateFunction((prevState) => handleReactStateChange(prevState, channel, stream));

  // Disable every other stream of the module
  appStreams.forEach((ch) =>
    ch.appStreams[appStream.name]
      ?.filter((as) => getStreamName(as) !== stream && isStreamToBeEnabled(channel, as, toEnable, toDisable))
      .forEach((as) =>
        handleModuleEnableDisable(ch.channel, as, appStreams, toEnable, toDisable, setToEnable, setToDisable)
      )
  );
};

/**
 * This function updates the toEnable or toDisable Map based on the appStream being handled.
 *
 * As we aim to switch the current state, it checks if the stream is already in the list of
 * enabled/disabled streams for the channel.
 *
 * If the stream already there, it is removed (toggle off), otherwise it is included (toggle on).
 *
 * A new Map instance is created based on the previous state to avoid direct mutation.
 */
const handleReactStateChange = (prevState: Map<number, string[]>, channel: Channel, stream: string) => {
  const updatedMap = new Map<number, string[]>(prevState);
  let newList = updatedMap.get(channel.id) ?? [];
  newList = newList.includes(stream) ? newList.filter((it) => it !== stream) : newList.concat(stream);
  updatedMap.set(channel.id, newList);
  return updatedMap;
};

/**
 * Determines if a given @param stream is to be enabled for a specified @param channel,
 * considering both the backend status (stream.enabled) and the frontend changes at a
 * particular moment (@param toEnable and @param toDisable).
 *
 * @returns true if the stream is to be enabled, otherwise false.
 */
const isStreamToBeEnabled = (
  channel: Channel,
  stream: AppStreamModule,
  toEnable: Map<number, string[]>,
  toDisable: Map<number, string[]>
) =>
  toEnable.get(channel.id)?.includes(getStreamName(stream)) ||
  (stream.enabled && !toDisable.get(channel.id)?.includes(getStreamName(stream)));

/**
 * Calculates the total number of changes by summing the lengths of the lists in the toEnable and toDisable maps.
 *
 * @param toEnable - Map where keys are channel IDs and values are lists of streams to be enabled.
 * @param toDisable - Map where keys are channel IDs and values are lists of streams to be disabled.
 * @returns The total number of changes (streams to be enabled or disabled).
 */
export const numberOfChanges = (toEnable: Map<number, string[]>, toDisable: Map<number, string[]>) => {
  return reduceMapListLength(toEnable) + reduceMapListLength(toDisable);
};

/**
 * Sums the lengths of the lists in the provided map.
 *
 * @param mapToReduce - Map where keys are channel IDs and values are lists of streams.
 * @returns The total length of all lists combined in the map.
 */
const reduceMapListLength = (mapToReduce: Map<number, string[]>) => {
  return Array.from(mapToReduce.keys()).reduce((acc, key) => acc + (mapToReduce.get(key)?.length ?? 0), 0);
};
