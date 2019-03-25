// @flow
import useMandatoryChannelsApi from "./use-mandatory-channels-api";

type ChannelDto = {
  id: number,
  name: string,
  custom: boolean,
  subscribable: boolean,
  recommended: boolean
}


type Props = {
  base: Object,
  channels: Array<ChannelDto>,
  children: Function,
}

// Just a wrapper to use the react-hook useMandatoryChannelsApi in old Code using classes as render-props.
function MandatoryChannelsApi({ base, channels, children }: Props) {
  let apiHookReturn = useMandatoryChannelsApi({base, channels})
  return children(apiHookReturn)
}


export default MandatoryChannelsApi;
