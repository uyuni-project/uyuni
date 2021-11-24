// TODO: This is obsolete
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
  mandatory: unknown[];
  name: string;
  standardizedName: string;
  recommended: boolean;
  subscribable: boolean;
};

export type DerivedBaseChannel = DerivedChannelPartial & {
  isOpen: boolean;
  children: (DerivedChannelPartial & {
    parent: DerivedBaseChannel;
  })[];
};
