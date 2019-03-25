// @flow
export type ChannelType = {
  id: number,
  name: string,
  archLabel: string,
  custom: boolean,
  isCloned: boolean,
  subscribable: boolean,
  recommended: boolean,
  compatibleChannelPreviousSelection?: number,
  children: Array<number>
}
