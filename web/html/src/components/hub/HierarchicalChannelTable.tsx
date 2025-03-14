import * as React from "react";

import { CustomDiv } from "components/custom-objects";
import { Channel } from "components/hub/types";
import { Form, Select } from "components/input";
import { Messages, MessageType } from "components/messages/messages";
import { CustomDataHandler } from "components/table/CustomDataHandler";
import { SearchField } from "components/table/SearchField";

const _COLS = {
  selector: { width: 5, um: "%" },
  showSubList: { width: 5, um: "%" },
  name: { width: 35, um: "%" },
  label: { width: 35, um: "%" },
  arch: { width: 7, um: "%" },
  org: { width: 13, um: "%" },
};

// Define search criteria for the search field
const searchCriteriaInChannel = (channel, criteria) => {
  if (!criteria) return true;
  const lowerCriteria = criteria.toLowerCase();
  return (
    channel.channelName.toLowerCase().includes(lowerCriteria) ||
    channel.channelLabel.toLowerCase().includes(lowerCriteria) ||
    (channel.channelArch && channel.channelArch.toLowerCase().includes(lowerCriteria))
  );
};

interface HierarchicalChannelTableProps {
  data: Channel[];
  loading?: boolean;
  readOnlyMode?: boolean;
  selectedItems?: Channel[];
  handleSelectedItems?: (items: Channel[]) => void;
  handleUnselectedItems?: (items: Channel[]) => void;
}

interface HierarchicalChannelTableState {
  archCriteria: string[];
  visibleSubList: number[];
  errors: MessageType[];
}

export class HierarchicalChannelTable extends React.Component<
  HierarchicalChannelTableProps,
  HierarchicalChannelTableState
> {
  constructor(props: HierarchicalChannelTableProps) {
    super(props);
    this.state = {
      archCriteria: [],
      visibleSubList: [],
      errors: [],
    };
  }

  // Get distinct architectures from the data for filtering
  getDistinctArchsFromData = (data: Channel[] = []) => {
    const collectArchs = (channels: Channel[]): string[] => {
      let archs: string[] = [];
      channels.forEach((channel) => {
        if (channel.channelArch) archs.push(channel.channelArch);
        if (channel.children && channel.children.length > 0) {
          archs = archs.concat(collectArchs(channel.children));
        }
      });
      return archs;
    };

    return Array.from(new Set(collectArchs(data)))
      .filter(Boolean)
      .sort();
  };

  // Filter data by architecture
  filterDataByArch = (data: Channel[]) => {
    if (this.state.archCriteria.length === 0) return data;

    const filterChannel = (channel: Channel): Channel | null => {
      // Include the channel if its architecture matches
      const matchesArch = !channel.channelArch || this.state.archCriteria.includes(channel.channelArch);

      // Filter children
      let filteredChildren: Channel[] = [];
      if (channel.children && channel.children.length > 0) {
        channel.children.forEach((child) => {
          const filteredChild = filterChannel(child);
          if (filteredChild) filteredChildren.push(filteredChild);
        });
      }

      // Return the channel with filtered children if it matches or has matching children
      if (matchesArch || filteredChildren.length > 0) {
        return {
          ...channel,
          children: filteredChildren,
        };
      }

      return null;
    };

    // Apply the filter to each channel in the data
    return data.map(filterChannel).filter((channel): channel is Channel => channel !== null);
  };

  // Handle selection of items
  handleSelectedItems = (items: Channel[]) => {
    if (this.props.handleSelectedItems) {
      this.props.handleSelectedItems(items);
    }
  };

  // Handle unselection of items
  handleUnselectedItems = (items: Channel[]) => {
    if (this.props.handleUnselectedItems) {
      this.props.handleUnselectedItems(items);
    }
  };

  // Compare channels for sorting
  compareChannels = (chan1: Channel, chan2: Channel) => {
    // First sort by parent/child status
    if (!chan1.parentId && chan2.parentId) return -1;
    if (chan1.parentId && !chan2.parentId) return 1;

    // Then sort by name
    return chan1.channelName.toLowerCase().localeCompare(chan2.channelName.toLowerCase());
  };

  // Convert channels to rows for the data handler
  buildRows = (channels: Channel[]) => {
    return channels;
  };

  // Toggle visibility of sublists
  handleVisibleSublist = (id: number) => {
    let arr = [...this.state.visibleSubList];
    if (arr.includes(id)) {
      arr = arr.filter((i) => i !== id);
    } else {
      arr = arr.concat([id]);
    }
    this.setState({ visibleSubList: arr });
  };

  render() {
    const { data, loading, readOnlyMode } = this.props;
    const selectedItems = this.props.selectedItems || [];

    const archFilter = (
      <div className="multiple-select-wrapper table-input-search">
        <Form>
          <Select
            name="channel-arch-filter"
            placeholder={t("Filter by architecture")}
            options={this.getDistinctArchsFromData(data)}
            isMulti
            onChange={(_, archCriteria) => this.setState({ archCriteria })}
          />
        </Form>
      </div>
    );

    return (
      <div>
        <Messages items={this.state.errors} />
        <CustomDataHandler
          data={this.buildRows(this.filterDataByArch([...data]).sort(this.compareChannels))}
          identifier={(raw) => raw.channelId}
          loading={loading}
          additionalFilters={[archFilter]}
          searchField={
            <SearchField
              filter={searchCriteriaInChannel}
              placeholder={t("Filter by channel name or label")}
              name="product-name-filter"
            />
          }
        >
          <ChannelList
            data={(data) => data}
            bypassProps={{
              nestedKey: "children",
              isSelectable: true,
              selectedItems: selectedItems,
              listStyleClass: "product-list",
              cols: _COLS,
              handleVisibleSublist: this.handleVisibleSublist,
              visibleSubList: this.state.visibleSubList,
              readOnlyMode: readOnlyMode,
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
  data: Channel[] | ((data: any) => Channel[]);
  bypassProps: any;
  treeLevel: number;
  childrenDisabled?: boolean;
  handleSelectedItems: (items: Channel[]) => void;
  handleUnselectedItems: (items: Channel[]) => void;
}

/**
 * Generate a custom list of elements for the channels data
 */
class ChannelList extends React.Component<ChannelListProps> {

  render() {
    // Handle both cases: data as array or data as function
    const channelData = typeof this.props.data === "function" ? this.props.data(this.props) : this.props.data;

    if (!channelData || channelData.length === 0) {
      return null;
    }

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
              className="col col-name-width"
              width={this.props.bypassProps.cols.label.width}
              um={this.props.bypassProps.cols.label.um}
            >
              {t("Channel Label")}
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
          </li>
        ) : null}
        {channelData.map((l, index) => {
          return (
            <ChannelListItem
              key={l.channelId}
              item={l}
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
  item: Channel;
  bypassProps: any;
  treeLevel: number;
  childrenDisabled?: boolean;
  index: number;
  handleSelectedItems: (items: Channel[]) => void;
  handleUnselectedItems: (items: Channel[]) => void;
}

/**
 * A component to generate a list item which contains
 * all information for a single channel
 */
class ChannelListItem extends React.Component<ChannelListItemProps> {
  isSelected = (item: Channel, selectedItems: Channel[]) => {
    return selectedItems.filter((i) => i.channelId === item.channelId).length === 1;
  };

  isSublistVisible = () => {
    return this.props.bypassProps.visibleSubList.includes(this.props.item.channelId);
  };

  handleSelectedItem = () => {
    const currentItem = this.props.item;

    // Add the current channel
    let arr = [this.props.item];

    // This item was selected but it is going to be removed from the selected set,
    // so all children are going to be removed as well
    if (this.isSelected(currentItem, this.props.bypassProps.selectedItems)) {
      arr = arr.concat(this.getChildrenTree(currentItem));
      this.handleUnselectedItems(arr);
    } else {
      // This item was not selected and it is going to be added to the selected set
      this.handleSelectedItems(arr);
    }
  };

  getChildrenTree = (item: Channel) => {
    let arr = this.getNestedData(item);
    let nestedArr: Channel[] = [];
    arr.forEach((child: Channel) => {
      nestedArr = nestedArr.concat(this.getChildrenTree(child));
    });
    return arr.concat(nestedArr);
  };

  handleSelectedItems = (items: Channel[]) => {
    this.props.handleSelectedItems(items);
  };

  handleUnselectedItems = (items: Channel[]) => {
    this.props.handleUnselectedItems(items);
  };

  getNestedData = (item: Channel) => {
    if (item && this.props.bypassProps.nestedKey && item[this.props.bypassProps.nestedKey] != null) {
      return item[this.props.bypassProps.nestedKey];
    }
    return [];
  };

  render() {
    const currentItem = this.props.item;

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
          disabled={this.props.bypassProps.readOnlyMode || this.props.childrenDisabled}
          title={
            this.props.childrenDisabled
              ? t("To enable this channel, the parent channel should be selected first")
              : t("Select this channel")
          }
        />
      );
    }

    /** Generate show nested list icon **/
    let showNestedDataIconContent: {} | null | undefined;
    if (this.getNestedData(currentItem).length > 0) {
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
    if (this.getNestedData(currentItem).length > 0) {
      handleNameClick = () => this.props.bypassProps.handleVisibleSublist(currentItem.channelId);
      hoverableNameClass = "product-hover pointer";
    }
    let channelNameContent = (
      <span className={"product-name " + hoverableNameClass} onClick={handleNameClick}>
        {currentItem.channelName}
      </span>
    );
    let channelLabelContent = <span className={"product-name"}>{currentItem.channelLabel}</span>;

    const evenOddClass = this.props.index % 2 === 0 ? "list-row-even" : "list-row-odd";
    const indentClass = this.props.treeLevel > 1 ? "nested-product-item" : "";

    return (
      <li
        className={`${evenOddClass} ${this.isSublistVisible() ? "sublistOpen" : ""} ${indentClass}`}
        key={currentItem.channelId}
      >
        <div className="product-details-wrapper" data-identifier={currentItem.channelId}>
          <CustomDiv
            className="col text-center"
            width={this.props.bypassProps.cols.selector.width}
            um={this.props.bypassProps.cols.selector.um}
          >
            {selectorContent}
          </CustomDiv>
          {this.props.treeLevel === 0 ? (
            <CustomDiv
              className="col text-center"
              width={this.props.bypassProps.cols.showSubList.width}
              um={this.props.bypassProps.cols.showSubList.um}
            >
              {showNestedDataIconContent}
            </CustomDiv>
          ) : null}
          <CustomDiv
            className="col col-name-width"
            width={"" + (this.props.bypassProps.cols.name.width + 2)}
            um={this.props.bypassProps.cols.name.um}
          >
            {channelNameContent}
          </CustomDiv>
          <CustomDiv
            className="col col-name-width"
            width={this.props.bypassProps.cols.label.width}
            um={this.props.bypassProps.cols.label.um}
          >
            {channelLabelContent}
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
          >
            {currentItem.channelOrg ? currentItem.channelOrg.orgName : "SUSE"}
          </CustomDiv>
        </div>
        {this.isSublistVisible() ? (
          <ChannelList
            data={this.getNestedData(currentItem)}
            bypassProps={this.props.bypassProps}
            handleSelectedItems={this.handleSelectedItems}
            handleUnselectedItems={this.handleUnselectedItems}
            treeLevel={this.props.treeLevel + 1}
            childrenDisabled={!this.isSelected(currentItem, this.props.bypassProps.selectedItems)}
          />
        ) : null}
      </li>
    );
  }
}
