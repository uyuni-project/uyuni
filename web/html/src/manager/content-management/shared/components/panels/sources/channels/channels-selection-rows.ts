import { DerivedBaseChannel, DerivedChildChannel } from "core/channels/type/channels.type";

export enum RowType {
  Parent,
  Child,
  EmptyChild,
  RecommendedToggle,
}

// TODO: Rename to Base
type ParentDefinition = {
  type: RowType.Parent;
  channel: DerivedBaseChannel;
  isOpen: boolean;
  isSelected: boolean;
  isSelectedBaseChannel: boolean;
  selectedChildrenCount: number;
};

type ChildDefinition = {
  type: RowType.Child;
  isSelected: boolean;
  isRequired: boolean;
  channel: DerivedChildChannel;
};

type EmptyChildDefinition = {
  type: RowType.EmptyChild;
};

type RecommendedToggleDefinition = {
  type: RowType.RecommendedToggle;
  channel: DerivedBaseChannel;
  areAllRecommendedChildrenSelected: boolean;
};

export type RowDefinition = {
  // This identifier is used as the key in the list
  id: string | number;
} & (ParentDefinition | ChildDefinition | EmptyChildDefinition | RecommendedToggleDefinition);
