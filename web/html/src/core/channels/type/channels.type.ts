export type ChannelTypePartial = {
  id: number;
  archLabel: string;
  custom: boolean;
  isCloned: boolean;
  label: string;
  name: string;
  recommended: boolean;
  subscribable: boolean;
};

export type MandatoryChannel = ChannelTypePartial & {
  compatibleChannelPreviousSelection?: number;
  children: number[];
};

export type BaseChannelType = ChannelTypePartial & {
  // These are fields which we compute and tack on, they don't exist originally
  standardizedName: string;
  recommendedChildren: ChildChannelType[];
};

export type ChildChannelType = ChannelTypePartial & {
  standardizedName: string;
  parent: BaseChannelType;
};

export type ChannelTreeType = {
  base: BaseChannelType;
  children: ChildChannelType[];
};

export type ChannelType = BaseChannelType | ChildChannelType;

export function isBaseChannel(input: ChannelType): input is BaseChannelType {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "recommendedChildren"));
}

export function isChildChannel(input: ChannelType): input is ChildChannelType {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "parent"));
}
