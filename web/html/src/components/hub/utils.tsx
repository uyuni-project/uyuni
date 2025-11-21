import { SelectionProvider } from "components/table";

import { Channel } from "./types";

/**
 * Custom filters for the ChannelSelectorTable
 */
export const ChannelFilter = {
  byChannelName(channel: Channel, criteria: string | undefined): boolean {
    if (criteria === undefined) {
      return true;
    }

    if (channel.name.toLowerCase().includes(criteria.toLowerCase())) {
      return true;
    }

    if (channel.children.length > 0) {
      for (const child of channel.children) {
        if (ChannelFilter.byChannelName(child, criteria)) {
          return true;
        }
      }
    }

    return false;
  },

  byArchitecture(channel: Channel, archs: string[]): boolean {
    if (archs.length === 0) {
      return true;
    }

    return archs.includes(channel.architecture);
  },
};

/**
 * Custom {@link SelectionProvider} for customizing the selection behaviour for ChannelSelectorTable:
 * - on selecting, the node, it's parent and all the mandatory children are selected
 * - on deselecting, only the children are deselected
 * There is no need to recurse the children because the channel hierarchy can only have one level
 */
export class ParentMandatoryChildrenSelectionProvider implements SelectionProvider<Channel> {
  private readonly parentMap: Map<number, Channel>;

  private readonly requiresMap: Map<number, number[]>;

  private readonly requiredByMap: Map<number, number[]>;

  public constructor(
    parentMap: Map<number, Channel>,
    requiresMap: Map<number, number[]>,
    requiredByMap: Map<number, number[]>
  ) {
    this.parentMap = parentMap;
    this.requiresMap = requiresMap;
    this.requiredByMap = requiredByMap;
  }

  public select(item: Channel): Channel[] {
    return this.getRelatedChannels(item, this.requiresMap);
  }

  public unselect(item: Channel): Channel[] {
    return this.getRelatedChannels(item, this.requiredByMap);
  }

  private getRelatedChannels(item: Channel, dependencyMap: Map<number, number[]>): Channel[] {
    const baseChannel = this.getParentOrSelf(item);
    const allChannels = [baseChannel, ...baseChannel.children];

    // Look up IDs in the passed map (either requires or requiredBy)
    const targetIds = dependencyMap.get(item.id) ?? [];
    const matchedChildren = allChannels.filter((child) => targetIds.includes(child.id));

    return [item, ...matchedChildren];
  }

  private getParentOrSelf(item: Channel): Channel {
    if (item.parentId === null) {
      return item;
    }

    return this.parentMap.get(item.parentId) ?? item;
  }
}
