// Every row must be directly identifiable by the virtual list
import { Identifiable } from "components/virtual-list/VirtualList";

export enum RowType {
  Parent,
  Child,
  EmptyChild,
  RecommendedToggle,
}

export type BaseRowDefinition = Identifiable & {
  type: RowType.Parent;
  id: number;
  channelName: string;
  isOpen: boolean;
  isSelected: boolean;
  isSelectedBaseChannel: boolean;
  selectedChildrenCount: number;
};

export type ChildRowDefinition = Identifiable & {
  type: RowType.Child;
  id: number;
  channelName: string;
  isSelected: boolean;
  isRecommended: boolean;
  isRequired: boolean;
  isRequiredBySelectedBaseChannel: boolean;
  tooltipData: {
    requiresNames: string[];
    requiredByNames: string[];
  };
};

export type EmptyChildRowDefinition = Identifiable & {
  type: RowType.EmptyChild;
  id: string; // The id is a merged string here to avoid collisions
};

export type RecommendedToggleRowDefinition = Identifiable & {
  type: RowType.RecommendedToggle;
  id: string; // The id is a merged string here to avoid collisions
  channelId: number; // TODO: Make this obsolete
  areAllRecommendedChildrenSelected: boolean;
};

export type RowDefinition =
  | BaseRowDefinition
  | ChildRowDefinition
  | EmptyChildRowDefinition
  | RecommendedToggleRowDefinition;
