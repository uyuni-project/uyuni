import * as React from "react";

import { CustomDiv } from "components/custom-objects";
import { Form, Select } from "components/input";
import { Messages, MessageType } from "components/messages/messages";
import { CustomDataHandler } from "components/table/CustomDataHandler";
import { SearchField } from "components/table/SearchField";

import { createChannelMap, FlatChannel } from "./ChannelFlatteningUtils";
import { DesyncChannel } from "./DesyncChannel";

const _COLS = {
  selector: { width: 2, um: "em" },
  showSubList: { width: 2, um: "em" },
  name: { width: 40, um: "em" },
  arch: { width: 7, um: "em" },
  org: { width: 13, um: "em" },
  synced: { width: 5, um: "em" },
  removeSynced: { width: 8, um: "em" },
};

// For checking if a channel matches search criteria
const searchCriteriaInChannel = (channel: FlatChannel, criteria: string | undefined): boolean => {
  if (!criteria) return true;
  const lowerCriteria = criteria.toLowerCase();
  return (
    channel.channelName.toLowerCase().includes(lowerCriteria) ||
    channel.channelLabel.toLowerCase().includes(lowerCriteria) ||
    (channel.channelArch?.toLowerCase().includes(lowerCriteria) ?? false)
  );
};

interface HierarchicalChannelTableProps {
  peripheralId: number;
  channels: FlatChannel[];
  loading?: boolean;
  handleSelectedItems?: (items: FlatChannel[]) => void;
  handleUnselectedItems?: (items: FlatChannel[]) => void;
}

interface HierarchicalChannelTableState {
  archCriteria: string[];
  visibleSubList: number[];
  errors: MessageType[];
  selectedItems: FlatChannel[];
  flatChannels: FlatChannel[];
  channelMap: Map<string, FlatChannel>;
  currentSearchCriteria?: string;
  rootChannels: FlatChannel[]; // Root-level channels
}

export class HierarchicalChannelTable extends React.Component<
  HierarchicalChannelTableProps,
  HierarchicalChannelTableState
> {
  constructor(props: HierarchicalChannelTableProps) {
    super(props);

    // Convert to flat channels if necessary
    const flatChannels = props.channels;
    const channelMap = createChannelMap(flatChannels);

    // Get root channels (those without a parent)
    const rootChannels = flatChannels.filter((channel) => !channel.parentChannelLabel);

    this.state = {
      archCriteria: [],
      visibleSubList: [],
      errors: [],
      selectedItems: [],
      flatChannels,
      channelMap,
      currentSearchCriteria: undefined,
      rootChannels,
    };
  }

  componentDidUpdate(prevProps: HierarchicalChannelTableProps) {
    if (prevProps.channels !== this.props.channels) {
      // Update flat channels and channel map
      const flatChannels = this.props.channels;
      const channelMap = createChannelMap(flatChannels);
      const rootChannels = flatChannels.filter((channel) => !channel.parentChannelLabel);

      this.setState({
        flatChannels,
        channelMap,
        rootChannels,
      });
    }
  }

  getDistinctArchsFromData = (flatChannels: FlatChannel[] = []) => {
    return Array.from(new Set(flatChannels.map((channel) => channel.channelArch)))
      .filter(Boolean)
      .sort();
  };

  // Check if a channel or any of its descendants match the search criteria
  channelHasMatchingDescendants = (channelLabel: string, searchCriteria: string): boolean => {
    const { flatChannels } = this.state;

    // Find direct children
    const children = flatChannels.filter((ch) => ch.parentChannelLabel === channelLabel);

    // Check if any child matches directly
    for (const child of children) {
      if (searchCriteriaInChannel(child, searchCriteria)) {
        return true;
      }

      // Recursively check grandchildren
      if (this.channelHasMatchingDescendants(child.channelLabel, searchCriteria)) {
        return true;
      }
    }

    return false;
  };

  // Filter channels by architecture criteria
  filterChannelsByArch = (channels: FlatChannel[]): FlatChannel[] => {
    const { archCriteria } = this.state;

    if (archCriteria.length === 0) return channels;

    return channels.filter((channel) => !channel.channelArch || archCriteria.includes(channel.channelArch));
  };

  handleSelectedItems = (items: FlatChannel[]) => {
    let newSelectedItems = [...this.state.selectedItems];
    const { channelMap } = this.state;

    items.forEach((item) => {
      if (this.isItemSelected(item, newSelectedItems)) {
        return;
      }

      newSelectedItems.push(item);

      // If this channel has a parent, select the parent too
      if (item.parentChannelLabel) {
        const parentChannel = channelMap.get(item.parentChannelLabel);
        if (parentChannel && !this.isItemSelected(parentChannel, newSelectedItems)) {
          this.selectParentChannel(parentChannel, newSelectedItems);
        }
      }

      // If this channel has an original, select the original too
      if (item.originalChannelLabel) {
        this.selectOriginalChannel(item, newSelectedItems);
      }
    });

    this.setState({ selectedItems: newSelectedItems });

    if (this.props.handleSelectedItems) {
      this.props.handleSelectedItems(newSelectedItems);
    }
  };

  handleUnselectedItems = (items: FlatChannel[]) => {
    const itemIdsToRemove = items.map((item) => item.channelId);
    const newSelectedItems = this.state.selectedItems.filter((item) => !itemIdsToRemove.includes(item.channelId));

    this.setState({ selectedItems: newSelectedItems });

    if (this.props.handleUnselectedItems) {
      this.props.handleUnselectedItems(items);
    }
  };

  private isItemSelected(item: FlatChannel, selectedItems: FlatChannel[]) {
    return selectedItems.some((selectedItem) => selectedItem.channelId === item.channelId);
  }

  private selectParentChannel(parentChannel: FlatChannel, selectedItems: FlatChannel[]) {
    if (!this.isItemSelected(parentChannel, selectedItems)) {
      selectedItems.push(parentChannel);

      // Recursively select parent's parent if it exists
      if (parentChannel.parentChannelLabel) {
        const grandparentChannel = this.state.channelMap.get(parentChannel.parentChannelLabel);
        if (grandparentChannel) {
          this.selectParentChannel(grandparentChannel, selectedItems);
        }
      }

      // Process parent's original if it exists
      if (parentChannel.originalChannelLabel) {
        this.selectOriginalChannel(parentChannel, selectedItems);
      }
    }
  }

  private selectOriginalChannel(channel: FlatChannel, selectedItems: FlatChannel[]) {
    if (!channel.originalChannelLabel) {
      return;
    }

    const lastOriginalLabel = this.findLastOriginalLabel(channel);
    if (lastOriginalLabel === channel.channelLabel) {
      return;
    }

    const originalChannel = this.state.channelMap.get(lastOriginalLabel);
    if (originalChannel && !this.isItemSelected(originalChannel, selectedItems)) {
      selectedItems.push(originalChannel);
    }
  }

  private findLastOriginalLabel(channel: FlatChannel, visitedLabels: Set<string> = new Set()): string {
    visitedLabels.add(channel.channelLabel);

    if (!channel.originalChannelLabel || channel.originalChannelLabel === channel.channelLabel) {
      return channel.channelLabel;
    }

    if (visitedLabels.has(channel.originalChannelLabel)) {
      return channel.channelLabel;
    }

    const originalChannel = this.state.channelMap.get(channel.originalChannelLabel);
    if (!originalChannel) {
      return channel.originalChannelLabel;
    }

    return this.findLastOriginalLabel(originalChannel, visitedLabels);
  }

  // Compare channels for sorting
  compareChannels = (chan1: FlatChannel, chan2: FlatChannel) => {
    // First sort by parent/child status (root channels first)
    if (!chan1.parentChannelLabel && chan2.parentChannelLabel) return -1;
    if (chan1.parentChannelLabel && !chan2.parentChannelLabel) return 1;
    // Then sort by name
    return chan1.channelName.toLowerCase().localeCompare(chan2.channelName.toLowerCase());
  };

  handleVisibleSublist = (id: number) => {
    let arr = [...this.state.visibleSubList];
    if (arr.includes(id)) {
      arr = arr.filter((i) => i !== id);
    } else {
      arr = arr.concat([id]);
    }
    this.setState({ visibleSubList: arr });
  };

  handleSearch = (searchCriteria: string | undefined) => {
    if (!searchCriteria) {
      // If clearing search, reset state
      this.setState({
        currentSearchCriteria: undefined,
        visibleSubList: [],
      });
      return;
    }

    // Store search criteria
    this.setState({ currentSearchCriteria: searchCriteria }, () => {
      const { flatChannels } = this.state;
      // Find all channels that match search criteria directly (for auto-expanding)
      const directMatchingChannels = flatChannels.filter((channel) => searchCriteriaInChannel(channel, searchCriteria));
      // Create set for parent channel IDs that need to be expanded
      const visibleSubList = new Set(this.state.visibleSubList);
      // For each matching channel, add all its parent channels to visibleSubList
      directMatchingChannels.forEach((channel) => {
        if (channel.parentChannelLabel) {
          let currentLabel: string | null = channel.parentChannelLabel;
          while (currentLabel) {
            const parentChannel = this.state.channelMap.get(currentLabel);
            if (parentChannel) {
              visibleSubList.add(parentChannel.channelId);
              currentLabel = parentChannel.parentChannelLabel || null;
            } else {
              break;
            }
          }
        }
      });
      // Update state to expand relevant nodes
      this.setState({
        visibleSubList: Array.from(visibleSubList),
      });
    });
  };

  handleArchFilterChange = (archCriteria: string[]) => {
    this.setState({ archCriteria });
  };

  render() {
    const { loading } = this.props;
    const { flatChannels, selectedItems, currentSearchCriteria } = this.state;
    const filteredByArch = this.filterChannelsByArch(flatChannels);

    const archFilter = (
      <div className="multiple-select-wrapper table-input-search">
        <Form>
          <Select
            name="channel-arch-filter"
            placeholder={t("Filter by architecture")}
            options={this.getDistinctArchsFromData(flatChannels)}
            isMulti
            onChange={(_, archCriteria) => this.handleArchFilterChange(archCriteria)}
          />
        </Form>
      </div>
    );

    return (
      <div>
        <Messages items={this.state.errors} />
        <CustomDataHandler
          data={filteredByArch} // Pass all channels filtered by architecture
          identifier={(raw) => raw.channelId}
          loading={loading}
          additionalFilters={[archFilter]}
          searchField={
            <SearchField
              filter={searchCriteriaInChannel} // Use standard search filter that works on all channels
              placeholder={t("Filter by channel name or label")}
              name="product-name-filter"
              onSearch={this.handleSearch}
            />
          }
        >
          <ChannelList
            data={(data) => {
              const searchCriteria = data.criteria;
              // Filter to only show root channels
              const rootOnly = data.filter((channel) => !channel.parentChannelLabel);
              if (searchCriteria) {
                const matchingRoots = rootOnly.filter((rootChannel) => {
                  // Direct match
                  if (searchCriteriaInChannel(rootChannel, searchCriteria)) {
                    return true;
                  }
                  // Check for matching descendants
                  return this.channelHasMatchingDescendants(rootChannel.channelLabel, searchCriteria);
                });
                return matchingRoots.sort(this.compareChannels);
              }
              // Otherwise, just return all root channels
              return rootOnly.sort(this.compareChannels);
            }}
            peripheralId={this.props.peripheralId}
            bypassProps={{
              allChannels: filteredByArch, // Need all channels for child lookups
              channelMap: this.state.channelMap,
              isSelectable: true,
              selectedItems: selectedItems,
              listStyleClass: "product-list",
              cols: _COLS,
              handleVisibleSublist: this.handleVisibleSublist,
              visibleSubList: this.state.visibleSubList,
              searchCriteria: currentSearchCriteria,
            }}
            handleSelectedItems={this.handleSelectedItems}
            handleUnselectedItems={this.handleUnselectedItems}
            treeLevel={0}
            childrenDisabled={false}
          />
        </CustomDataHandler>
      </div>
    );
  }
}

interface ChannelListProps {
  data: FlatChannel[] | ((data: any) => FlatChannel[]);
  peripheralId: number;
  bypassProps: {
    allChannels: FlatChannel[];
    channelMap: Map<string, FlatChannel>;
    isSelectable: boolean;
    selectedItems: FlatChannel[];
    listStyleClass: string;
    cols: any;
    handleVisibleSublist: (id: number) => void;
    visibleSubList: number[];
    searchCriteria?: string;
  };
  treeLevel: number;
  childrenDisabled?: boolean;
  handleSelectedItems: (items: FlatChannel[]) => void;
  handleUnselectedItems: (items: FlatChannel[]) => void;
}

/**
 * Generate a custom list of elements for the channels data
 */
class ChannelList extends React.Component<ChannelListProps> {
  render() {
    // Since we can have both data as array or data as function, handle it
    const channelData = typeof this.props.data === "function" ? this.props.data(this.props) : this.props.data;

    if (!channelData || channelData.length === 0) {
      return null;
    }

    const displayData =
      this.props.treeLevel === 0 ? channelData.filter((channel) => !channel.parentChannelLabel) : channelData;

    return (
      <ul className={this.props.bypassProps.listStyleClass}>
        {this.props.treeLevel === 0 ? (
          <li className="list-header">
            <CustomDiv
              className="col text-center"
              width={this.props.bypassProps.cols.selector.width}
              um={this.props.bypassProps.cols.selector.um}
            ></CustomDiv>
            <CustomDiv
              className="col text-center"
              width={this.props.bypassProps.cols.showSubList.width}
              um={this.props.bypassProps.cols.showSubList.um}
            ></CustomDiv>
            <CustomDiv
              className="col col-name-width"
              width={this.props.bypassProps.cols.name.width}
              um={this.props.bypassProps.cols.name.um}
            >
              {t("Channel Name")}
            </CustomDiv>
            <CustomDiv
              className="col"
              width={this.props.bypassProps.cols.arch.width}
              um={this.props.bypassProps.cols.arch.um}
              title={t("Architecture")}
            >
              {t("Arch")}
            </CustomDiv>
            <CustomDiv
              className="col"
              width={this.props.bypassProps.cols.org.width}
              um={this.props.bypassProps.cols.org.um}
            >
              {t("Organization")}
            </CustomDiv>
            <CustomDiv
              className="col"
              width={this.props.bypassProps.cols.synced.width}
              um={this.props.bypassProps.cols.synced.um}
              title={t("Synced")}
            >
              {t("Synced")}
            </CustomDiv>
            <CustomDiv
              className="col"
              width={this.props.bypassProps.cols.removeSynced.width}
              um={this.props.bypassProps.cols.removeSynced.um}
              title={t("Remove Sync")}
            >
              {t("Remove Sync")}
            </CustomDiv>
          </li>
        ) : null}
        {displayData.map((channel, index) => {
          return (
            <ChannelListItem
              key={channel.channelId}
              peripheralId={this.props.peripheralId}
              item={channel}
              bypassProps={this.props.bypassProps}
              handleSelectedItems={this.props.handleSelectedItems}
              handleUnselectedItems={this.props.handleUnselectedItems}
              treeLevel={this.props.treeLevel}
              childrenDisabled={this.props.childrenDisabled}
              index={index}
            />
          );
        })}
      </ul>
    );
  }
}

interface ChannelListItemProps {
  item: FlatChannel;
  peripheralId: number;
  bypassProps: {
    allChannels: FlatChannel[];
    channelMap: Map<string, FlatChannel>;
    isSelectable: boolean;
    selectedItems: FlatChannel[];
    listStyleClass: string;
    cols: any;
    handleVisibleSublist: (id: number) => void;
    visibleSubList: number[];
    searchCriteria?: string;
  };
  treeLevel: number;
  childrenDisabled?: boolean;
  index: number;
  handleSelectedItems: (items: FlatChannel[]) => void;
  handleUnselectedItems: (items: FlatChannel[]) => void;
}

/**
 * A component to generate a list item which contains
 * all information for a single channel
 */
class ChannelListItem extends React.Component<ChannelListItemProps> {
  isSelected = (item: FlatChannel, selectedItems: FlatChannel[]) => {
    return selectedItems.some((i) => i.channelId === item.channelId);
  };

  isSublistVisible = () => {
    return this.props.bypassProps.visibleSubList.includes(this.props.item.channelId);
  };

  handleSelectedItem = () => {
    const currentItem = this.props.item;
    // Add the current channel
    let arr = [this.props.item];
    // Remove selection from all children too
    if (this.isSelected(currentItem, this.props.bypassProps.selectedItems)) {
      arr = arr.concat(this.getChildrenTree(currentItem));
      this.handleUnselectedItems(arr);
    } else {
      // This item was not selected and it is going to be added to the selected set
      this.handleSelectedItems(arr);
    }
  };

  getChildrenTree = (item: FlatChannel) => {
    const { allChannels } = this.props.bypassProps;
    let allChildren: FlatChannel[] = [];
    const getChildren = (channelLabel: string) => {
      const directChildren = allChannels.filter((ch) => ch.parentChannelLabel === channelLabel);
      allChildren = allChildren.concat(directChildren);
      directChildren.forEach((child) => {
        getChildren(child.channelLabel);
      });
    };

    getChildren(item.channelLabel);
    return allChildren;
  };

  handleSelectedItems = (items: FlatChannel[]) => {
    this.props.handleSelectedItems(items);
  };

  handleUnselectedItems = (items: FlatChannel[]) => {
    this.props.handleUnselectedItems(items);
  };

  getNestedData = () => {
    const { allChannels } = this.props.bypassProps;
    const { item } = this.props;
    const criteria = this.props.bypassProps.searchCriteria;
    // Get all direct children (channels where parentChannelLabel matches this item's channelLabel)
    const children = allChannels.filter((ch) => ch.parentChannelLabel === item.channelLabel);
    // If there's search criteria, only show children that match or have matching descendants
    if (criteria) {
      return children.filter((child) => this.childMatchesOrHasMatchingDescendants(child, criteria));
    }
    return children;
  };

  childMatchesOrHasMatchingDescendants = (channel: FlatChannel, criteria: string): boolean => {
    const { allChannels } = this.props.bypassProps;
    // Check if this channel matches
    if (searchCriteriaInChannel(channel, criteria)) return true;
    // Check if any descendants match (recursive)
    const childrenMatch = (parentLabel: string): boolean => {
      const directChildren = allChannels.filter((ch) => ch.parentChannelLabel === parentLabel);
      return directChildren.some(
        (child) => searchCriteriaInChannel(child, criteria) || childrenMatch(child.channelLabel)
      );
    };
    return childrenMatch(channel.channelLabel);
  };

  render() {
    const currentItem = this.props.item;
    const childrenData = this.getNestedData();
    /** Generate item selector content **/
    let selectorContent: React.ReactNode = null;
    if (this.props.bypassProps.isSelectable) {
      selectorContent = (
        <input
          type="checkbox"
          id={"checkbox-for-" + currentItem.channelId}
          value={currentItem.channelId}
          onChange={this.handleSelectedItem}
          checked={this.isSelected(currentItem, this.props.bypassProps.selectedItems)}
          disabled={this.props.childrenDisabled || currentItem.synced}
          title={
            this.props.childrenDisabled
              ? t("To enable this channel, the parent channel should be selected first or unsync the channel")
              : t("Select this channel")
          }
        />
      );
    }
    /** Generate show nested list icon **/
    let showNestedDataIconContent: React.ReactNode = null;
    if (childrenData.length > 0) {
      const openSubListIconClass = this.isSublistVisible() ? "fa-angle-down" : "fa-angle-right";
      showNestedDataIconContent = (
        <i
          className={"fa " + openSubListIconClass + " fa-1-5x pointer product-hover"}
          onClick={() => this.props.bypassProps.handleVisibleSublist(currentItem.channelId)}
        />
      );
    }
    /** Generate channel name content **/
    let handleNameClick: (() => any) | undefined = undefined;
    let hoverableNameClass = "";
    if (childrenData.length > 0) {
      handleNameClick = () => this.props.bypassProps.handleVisibleSublist(currentItem.channelId);
      hoverableNameClass = "product-hover pointer";
    }
    let channelNameContent = (
      <span className={"product-name " + hoverableNameClass} onClick={handleNameClick}>
        {currentItem.channelName}
      </span>
    );

    const evenOddClass = this.props.index % 2 === 0 ? "list-row-even" : "list-row-odd";
    const indentClass = this.props.treeLevel > 0 ? "nested-product-item" : "";

    const channelDiv = (
      <div className="product-details-wrapper" data-identifier={currentItem.channelId}>
        <CustomDiv
          className="col text-center"
          width={this.props.bypassProps.cols.selector.width}
          um={this.props.bypassProps.cols.selector.um}
        >
          {selectorContent}
        </CustomDiv>
        <CustomDiv
          className="col text-center"
          width={this.props.bypassProps.cols.showSubList.width}
          um={this.props.bypassProps.cols.showSubList.um}
        >
          {showNestedDataIconContent}
        </CustomDiv>
        <CustomDiv
          className="col col-name-width"
          width={this.props.bypassProps.cols.name.width}
          um={this.props.bypassProps.cols.name.um}
          title={t("Name")}
        >
          {channelNameContent}
        </CustomDiv>
        <CustomDiv
          className="col"
          width={this.props.bypassProps.cols.arch.width}
          um={this.props.bypassProps.cols.arch.um}
          title={t("Architecture")}
        >
          {currentItem.channelArch || ""}
        </CustomDiv>
        <CustomDiv
          className="col"
          width={this.props.bypassProps.cols.org.width}
          um={this.props.bypassProps.cols.org.um}
          title={t("Organization")}
        >
          {currentItem.channelOrg ? currentItem.channelOrg.orgName : "SUSE"}
        </CustomDiv>
        <CustomDiv
          className="col"
          width={this.props.bypassProps.cols.synced.width}
          um={this.props.bypassProps.cols.synced.um}
          title={t("Synced Status")}
        >
          {currentItem.synced ? <i className="fa fa-check-circle"></i> : null}
        </CustomDiv>
        <CustomDiv
          className="col"
          width={this.props.bypassProps.cols.removeSynced.width}
          um={this.props.bypassProps.cols.removeSynced.um}
          title={t("Remove Sync")}
        >
          {currentItem.synced ? (
            <DesyncChannel
              label={this.props.item.channelLabel}
              name={this.props.item.channelName}
              peripheralId={this.props.peripheralId}
            />
          ) : null}
        </CustomDiv>
      </div>
    );

    return (
      <li
        className={`${evenOddClass} ${this.isSublistVisible() ? "sublistOpen" : ""} ${indentClass}`}
        key={currentItem.channelId}
      >
        {channelDiv}
        {this.isSublistVisible() && childrenData.length > 0 ? (
          <ChannelList
            data={childrenData}
            peripheralId={this.props.peripheralId}
            bypassProps={this.props.bypassProps}
            handleSelectedItems={this.handleSelectedItems}
            handleUnselectedItems={this.handleUnselectedItems}
            treeLevel={this.props.treeLevel + 1}
            childrenDisabled={!this.isSelected(currentItem, this.props.bypassProps.selectedItems) || currentItem.synced}
          />
        ) : null}
      </li>
    );
  }
}
