export type ChannelType = {
  id: number;
  name: string;
  label: string;
  archLabel: string;
  custom: boolean;
  isCloned: boolean;
  subscribable: boolean;
  recommended: boolean;
  compatibleChannelPreviousSelection?: number;
  children: Array<number>;
};
