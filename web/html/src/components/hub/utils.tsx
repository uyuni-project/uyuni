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

    if (channel.channelName.toLowerCase().includes(criteria.toLowerCase())) {
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

    return archs.includes(channel.channelArch);
  },
};

/**
 * Custom {@link SelectionProvider} for customizing the selection behaviour for ChannelSelectorTable:
 * - on selcting, both all the children and all the parents are selected
 * - on deselecting, only all the children are deselected
 */
export class ParentChildrenSelectionProvider implements SelectionProvider<Channel> {
  private readonly parentMap: Map<string, Channel>;

  public constructor(parentMap: Map<string, Channel>) {
    this.parentMap = parentMap;
  }

  public select(item: Channel): Channel[] {
    const result: Channel[] = [];

    result.push(item);
    result.push(...this.recurseParent(item));
    result.push(...this.recurseChildren(item));

    return result;
  }

  public unselect(item: Channel): Channel[] {
    const result: Channel[] = [];

    result.push(item);
    result.push(...this.recurseChildren(item));

    return result;
  }

  private recurseParent(item: Channel): Channel[] {
    if (item.parentChannelLabel === null) {
      return [];
    }

    const parent = this.parentMap.get(item.parentChannelLabel);
    if (parent === undefined) {
      return [];
    }

    const parents: Channel[] = [parent];
    parents.push(...this.recurseParent(parent));
    return parents;
  }

  private recurseChildren(item: Channel): Channel[] {
    const children: Channel[] = [];

    for (const child of item.children) {
      children.push(child);
      children.push(...this.recurseChildren(child));
    }

    return children;
  }
}
