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

// TODO: Better names for everything
export type ServerChannelType = {
  id: number;
  archLabel: string;
  custom: boolean;
  isCloned: boolean;
  label: string;
  name: string;
  recommended: boolean;
  subscribable: boolean;
};

export type RawChannelType = {
  base: ServerChannelType;
  children: ServerChannelType[];
};

export type DerivedChannelPartial = {
  id: number;
  archLabel: string;
  custom: boolean;
  isCloned: boolean;
  label: string;
  name: string;
  standardizedName: string;
  recommended: boolean;
  subscribable: boolean;
};

export type DerivedChildChannel = DerivedChannelPartial & {
  parent: DerivedBaseChannel;
};

export type DerivedBaseChannel = DerivedChannelPartial & {
  children: DerivedChildChannel[];
  recommendedChildrenIds: Set<number>;
};

export type DerivedChannel = DerivedBaseChannel | DerivedChildChannel;

export const isBaseChannel = (input: DerivedChannel | undefined): input is DerivedBaseChannel => {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "children"));
};

export const isChildChannel = (input: DerivedChannel | undefined): input is DerivedChildChannel => {
  return Boolean(input && Object.prototype.hasOwnProperty.call(input, "parent"));
};
