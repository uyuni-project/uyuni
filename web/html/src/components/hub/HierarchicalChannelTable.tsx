import * as React from "react";

import { CustomDataHandler } from "components/table/CustomDataHandler";
import { SearchField } from "components/table/SearchField";

import { Channel } from "./types";

interface HierarchicalChannelTableProps {
  data: Channel[];
  loading?: boolean;
}

interface HierarchicalChannelTableState {
  selectedItems: Channel[];
  expandedIds: number[]; // which channels are expanded
}

export class HierarchicalChannelTable extends React.Component<
  HierarchicalChannelTableProps,
  HierarchicalChannelTableState
> {
  constructor(props: HierarchicalChannelTableProps) {
    super(props);
    this.state = {
      selectedItems: [],
      expandedIds: [],
    };

    this.filterChannels = this.filterChannels.bind(this);
    this.toggleSelect = this.toggleSelect.bind(this);
    this.toggleExpand = this.toggleExpand.bind(this);
  }

  /**
   * Custom filter: checks channelName OR channelArch against the user-typed string.
   */
  filterChannels(row: Channel, criteria?: string): boolean {
    if (!criteria) return true;
    const needle = criteria.toLowerCase();
    // Check channel name
    const nameMatch = row.channelName.toLowerCase().includes(needle);
    return nameMatch;
  }

  /**
   * Toggle selection for a single channel.
   * If already selected, unselect it; otherwise select it.
   */
  toggleSelect(channel: Channel) {
    const { selectedItems } = this.state;
    const isSelected = selectedItems.some((sel) => sel.channelId === channel.channelId);

    if (isSelected) {
      // Unselect
      const newSelection = selectedItems.filter((sel) => sel.channelId !== channel.channelId);
      this.setState({ selectedItems: newSelection });
    } else {
      // Select
      this.setState({ selectedItems: [...selectedItems, channel] });
    }
  }

  /**
   * Toggle expand/collapse for a channel with children.
   */
  toggleExpand(channelId: number) {
    const { expandedIds } = this.state;
    if (expandedIds.includes(channelId)) {
      this.setState({
        expandedIds: expandedIds.filter((id) => id !== channelId),
      });
    } else {
      this.setState({ expandedIds: [...expandedIds, channelId] });
    }
  }

  render() {
    const { data, loading } = this.props;

    return (
      <div>
        <CustomDataHandler
          data={data}
          identifier={(ch: Channel) => ch.channelId}
          loading={loading}
          initialItemsPerPage={25}
          searchField={<SearchField placeholder={t("Filter by name or arch")} filter={this.filterChannels} />}
        >
          <table className="table table-hover table-striped">
            <thead>
              <tr>
                {/* Checkbox column */}
                <th style={{ width: "2em" }}></th>
                {/* Expand icon column */}
                <th style={{ width: "2em" }}></th>
                {/* Name column */}
                <th>{t("Name")}</th>
                {/* Architecture column */}
                <th style={{ width: "10em" }}>{t("Arch")}</th>
              </tr>
            </thead>
            <tbody>{this.renderRows(data, 0)}</tbody>
          </table>
        </CustomDataHandler>
      </div>
    );
  }

  /**
   * Recursively render <tr> rows for each channel + its children if expanded.
   * @param channels The array of channels to render at this level
   * @param depth How many levels deep we are (for indentation)
   * @returns An array of <tr> elements
   */
  private renderRows(channels: Channel[], depth: number): JSX.Element[] {
    const rows: JSX.Element[] = [];

    channels.forEach((channel) => {
      const isSelected = this.state.selectedItems.some((sel) => sel.channelId === channel.channelId);
      const hasChildren = channel.children && channel.children.length > 0;
      const isExpanded = this.state.expandedIds.includes(channel.channelId);

      rows.push(
        <tr key={channel.channelId}>
          {/* Checkbox column */}
          <td>
            <input type="checkbox" checked={isSelected} onChange={() => this.toggleSelect(channel)} />
          </td>

          {/* Expand/collapse column (only if children exist) */}
          <td>
            {hasChildren && (
              <i
                className={`fa fa-angle-${isExpanded ? "down" : "right"} pointer`}
                onClick={() => this.toggleExpand(channel.channelId)}
              />
            )}
          </td>

          {/* Name column, with indentation */}
          <td>
            <div style={{ marginLeft: depth * 20 }}>{channel.channelName}</div>
          </td>

          {/* Architecture column */}
          <td>{channel.channelArch || ""}</td>
        </tr>
      );

      // If expanded, render child rows
      if (hasChildren && isExpanded) {
        rows.push(...this.renderRows(channel.children!, depth + 1));
      }
    });

    return rows;
  }
}
