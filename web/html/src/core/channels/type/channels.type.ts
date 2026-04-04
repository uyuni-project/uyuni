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
  recommendedChildrenIds: number[];
};

export type ChildChannelType = ChannelTypePartial & {
  standardizedName: string;
  parentId: number;
};

export type ChannelTreeType = {
  base: BaseChannelType;
  children: ChildChannelType[];
};

export type ChannelType = BaseChannelType | ChildChannelType;

export function isBaseChannel(input: ChannelType): input is BaseChannelType {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "recommendedChildrenIds"));
}

export function isChildChannel(input: ChannelType): input is ChildChannelType {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "parentId"));
}
