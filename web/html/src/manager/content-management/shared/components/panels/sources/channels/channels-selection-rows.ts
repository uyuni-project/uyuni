import { DerivedBaseChannel, DerivedChildChannel } from "core/channels/type/channels.type";

export enum RowType {
  Parent,
  Child,
  EmptyChild,
  RecommendedToggle,
}

// TODO: Extend this
type BaseRowDefinition = {
  id: string | number; // This id is used as a key in the virtual list
};

// TODO: Rename to Base
export type ParentRowDefinition = {
  type: RowType.Parent;
  id: number;
  channelName: string;
  isOpen: boolean;
  isSelected: boolean;
  isSelectedBaseChannel: boolean;
  selectedChildrenCount: number;
};

export type ChildRowDefinition = {
  type: RowType.Child;
  id: number;
  channelName: string;
  isSelected: boolean;
  isRecommended: boolean;
  isRequired: boolean;
};

export type EmptyChildRowDefinition = {
  type: RowType.EmptyChild;
  id: string; // The id is a merged string here to avoid collisions
};

export type RecommendedToggleRowDefinition = {
  type: RowType.RecommendedToggle;
  id: string; // The id is a merged string here to avoid collisions
  channelId: number; // TODO: Make this obsolete
  areAllRecommendedChildrenSelected: boolean;
};

export type RowDefinition =
  | ParentRowDefinition
  | ChildRowDefinition
  | EmptyChildRowDefinition
  | RecommendedToggleRowDefinition;
