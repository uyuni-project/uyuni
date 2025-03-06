import * as React from "react";
import { useCallback, useMemo, useState } from "react";

import { Button } from "components/buttons";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { Channel } from "./types";

type Props = {
  // the full list of channels in hierarchical form
  channels: Channel[];
  // initially selected channels (by id)
  initialSelectedIds?: number[];
  // callback to notify parent of selection changes
  onSelectionChange?: (selectedIds: number[]) => void;
};

export const HierarchicalChannelTable: React.FC<Props> = ({ channels, initialSelectedIds = [], onSelectionChange }) => {
  // Store selected channel IDs in a local state, but notify parent if needed
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set(initialSelectedIds));
  // Store the search text for filtering
  const [searchTerm, setSearchTerm] = useState("");

  // A helper function to produce a flat list of channels from the hierarchical data. Preserve ordering: each parent followed by its children, recursively.
  const flattenChannels = useCallback((list: Channel[], depth = 0) => {
    let result: (Channel & { depth: number })[] = [];
    for (const ch of list) {
      // push the parent itself, along with a depth
      result.push({ ...ch, depth });
      // if it has children, flatten them recursively
      if (ch.children && ch.children.length > 0) {
        result = result.concat(flattenChannels(ch.children, depth + 1));
      }
    }
    return result;
  }, []);
  // Flatten once and store in a memo, so we don't do it on every render
  const flatChannels = useMemo(() => flattenChannels(channels), [channels, flattenChannels]);
  // Filter logic: if the searchTerm is non-empty, we want to keep any channel whose name includes the searchTerm (case-insensitive). We also want to keep the parent if the child matches, so the user can see the parent context.
  const filteredChannels = useMemo(() => {
    if (!searchTerm) {
      return flatChannels;
    }
    const lowerSearch = searchTerm.toLowerCase();
    // Two-pass approach: 1) Gather all channel IDs that match by name (or label if you prefer). 2) Also include the ancestors of those channels
    const matchingIds = new Set<number>();
    // Quick map from id -> channel for easy lookup
    const channelMap = new Map<number, Channel & { depth: number }>();
    flatChannels.forEach((ch) => channelMap.set(ch.channelId, ch));
    // Find matches by name
    for (const ch of flatChannels) {
      const nameMatch = ch.channelName.toLowerCase().includes(lowerSearch);
      if (nameMatch) {
        matchingIds.add(ch.channelId);
        // Also add all ancestors
        let current = ch;
        while (current.parentId) {
          matchingIds.add(current.parentId);
          const parent = channelMap.get(current.parentId);
          if (!parent) break;
          current = parent;
        }
      }
    }
    // Now filter the flatChannels so we only keep items in matchingIds
    return flatChannels.filter((ch) => matchingIds.has(ch.channelId));
  }, [searchTerm, flatChannels]);

  // Handler for toggling selection
  const handleRowSelection = useCallback(
    (channel: Channel) => {
      const newSelected = new Set(selectedIds);
      const isSelected = newSelected.has(channel.channelId);

      // If the channel is currently selected, we'll unselect it
      if (isSelected) {
        newSelected.delete(channel.channelId);
      } else {
        // If not selected, select it
        newSelected.add(channel.channelId);
        // If channel has a parent, we select the parent automatically
        if (channel.parentId) {
          newSelected.add(channel.parentId);
        }
      }
      setSelectedIds(newSelected);
      onSelectionChange?.(Array.from(newSelected));
    },
    [selectedIds, onSelectionChange]
  );

  // Function to see if a channel is selected
  const isChannelSelected = useCallback((id: number) => selectedIds.has(id), [selectedIds]);

  // Helper to get the indentation style for child channels
  const getIndentStyle = (depth: number) => ({
    paddingLeft: `${depth * 20}px`,
  });

  // Cell renderer for the name column that includes a checkbox
  const renderNameCell = (row: Channel & { depth: number }) => {
    return (
      <div style={{ display: "flex", alignItems: "center" }}>
        {/* Indent */}
        <div style={getIndentStyle(row.depth)}>
          <input type="checkbox" checked={isChannelSelected(row.channelId)} onChange={() => handleRowSelection(row)} />
          <span style={{ marginLeft: "8px" }}>{row.channelName}</span>
        </div>
      </div>
    );
  };

  const renderLabelCell = (row: Channel) => <span>{row.channelLabel}</span>;
  const renderArchCell = (row: Channel) => <span>{row.channelArch}</span>;
  const renderOrgCell = (row: Channel) => <span>{row.channelOrg ? row.channelOrg.orgName : "SUSE"}</span>;

  return (
    <div>
      <div style={{ marginBottom: "1rem" }}>
        <SearchField
          placeholder="Search channels by name"
          filter={(row, criteria) => true}
          onSearch={(val: string) => setSearchTerm(val)}
        />
      </div>
      <Table
        data={filteredChannels}
        identifier={(row: Channel) => row.channelId}
        selectable={false} // Handle selection manually
        initialSortColumnKey="channelName"
      >
        <Column
          columnKey="channelName"
          header="Name"
          cell={(row: Channel & { depth: number }) => renderNameCell(row)}
        />
        <Column columnKey="channelLabel" header="Label" cell={(row: Channel) => renderLabelCell(row)} />
        <Column columnKey="channelArch" header="Arch" cell={(row: Channel) => renderArchCell(row)} />
        <Column columnKey="org" header="Organization" cell={(row: Channel) => renderOrgCell(row)} />
      </Table>
      <div style={{ marginTop: "1rem" }}>
        <Button
          text="Print Selected"
          handler={() => alert(`Selected channel IDs: ${Array.from(selectedIds).join(", ")}`)}
        />
      </div>
    </div>
  );
};
