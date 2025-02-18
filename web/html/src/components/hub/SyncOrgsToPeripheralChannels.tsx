import * as React from "react";

import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { Utils } from "utils/functions";

// Types for organization and channel.
export type Org = {
  id: number;
  label: string;
};

export type Channel = {
  id: number;
  name: string;
  label: string;
};

// Mapping: organization ID → channels synced to that organization.
export type SyncedCustomChannels = {
  [orgId: number]: Channel[];
};

type State = {
  // The currently selected organization (or null if none is selected)
  selectedOrg: Org | null;
  // The channels that you selected for sync
  selectedCustomChannels: Channel[];
  // The channels that you selected to remove from sync
  selectedSyncedCustomChannels: Channel[];
  // For each organization, the channels that have been synced.
  syncedCustomChannels: SyncedCustomChannels;
  // Vendor channels have org null
  syncedVendorChannels: Channel[];
  // All the available orgs on the peripheral
  availableOrgs: Org[];
  // Custom channels not yet synced from hub
  availableCustomChannels: Channel[];
  // Vendor channels not yet synced from hub
  availableVendorChannels: Channel[];
};

export type SyncPeripheralsProps = {
  availableOrgs: Org[];
  availableCustomChannels: Channel[];
  availableVendorChannels: Channel[];
  syncedCustomChannels: SyncedCustomChannels;
  syncedVendorChannels: Channel[];
};

export class SyncOrgsToPeripheralChannel extends React.Component<SyncPeripheralsProps, State> {
  constructor(props: SyncPeripheralsProps) {
    super(props);
    this.state = {
      selectedOrg: null,
      selectedCustomChannels: [],
      selectedSyncedCustomChannels: [],
      syncedCustomChannels: props.syncedCustomChannels,
      syncedVendorChannels: props.syncedVendorChannels,
      availableOrgs: props.availableOrgs,
      availableCustomChannels: props.availableCustomChannels,
      availableVendorChannels: props.availableVendorChannels,
    };
  }

  /**
   * When the user selects an organization from the Organizations table.
   */
  handleOrgSelect(org: Org) {
    // When switching organizations, clear any previous channel selections.
    this.setState({
      selectedOrg: org,
      selectedCustomChannels: [],
      selectedSyncedCustomChannels: [],
    });
  }

  /**
   * Called when the user selects one or more rows in the Available Channels table.
   */
  handleCustomChannelSelect(channels: Channel[]) {
    // You can also write:
    // this.setState(prev => ({ selectedCustomChannels: [...prev.selectedCustomChannels, ...channels] }));
    this.setState({
      selectedCustomChannels: this.state.selectedCustomChannels.concat(...channels),
    });
  }

  /**
   * Called when the user selects one or more rows in the Synced Channels table.
   */
  handleSyncedCustomChannelSelect(channels: Channel[]) {
    this.setState({
      selectedSyncedCustomChannels: this.state.selectedSyncedCustomChannels.concat(...channels),
    });
  }

  /**
   * Moves channels selected in the Available table into the Synced table for the selected organization.
   */
  handleAddChannels() {
    const { selectedOrg, syncedCustomChannels, selectedCustomChannels, availableCustomChannels } = this.state;

    if (!selectedOrg) {
      alert("Please select an organization first.");
      return;
    }
    if (selectedCustomChannels.length === 0) {
      alert("Please select at least one channel to add.");
      return;
    }

    const orgId = selectedOrg.id;
    const currentSynced = syncedCustomChannels[orgId] || [];
    const newSynced = currentSynced.concat(selectedCustomChannels);

    // Remove only the selected channels from availableCustomChannels.
    const addedChannelIds = new Set(selectedCustomChannels.map((ch) => ch.id));
    const newAvailableCustomChannels = availableCustomChannels.filter((ch) => !addedChannelIds.has(ch.id));

    this.setState({
      syncedCustomChannels: {
        ...syncedCustomChannels,
        [orgId]: newSynced,
      },
      availableCustomChannels: newAvailableCustomChannels,
      selectedCustomChannels: [],
    });
  }

  /**
   * Moves channels selected in the Synced table back to the Available list.
   */
  handleRemoveChannels() {
    const { selectedOrg, syncedCustomChannels, selectedSyncedCustomChannels, availableCustomChannels } = this.state;

    if (!selectedOrg) {
      alert("Please select an organization first.");
      return;
    }
    if (selectedSyncedCustomChannels.length === 0) {
      alert("Please select at least one channel to remove.");
      return;
    }

    const orgId = selectedOrg.id;
    const currentSynced = syncedCustomChannels[orgId] || [];
    const remainingSynced = currentSynced.filter((ch) => !selectedSyncedCustomChannels.some((sel) => sel.id === ch.id));

    // Add the removed channels back into availableCustomChannels.
    const newAvailableCustomChannels = availableCustomChannels.concat(selectedSyncedCustomChannels);

    this.setState({
      syncedCustomChannels: {
        ...syncedCustomChannels,
        [orgId]: remainingSynced,
      },
      availableCustomChannels: newAvailableCustomChannels,
      selectedSyncedCustomChannels: [],
    });
  }

  render() {
    const {
      selectedOrg,
      syncedCustomChannels,
      availableOrgs,
      availableCustomChannels,
      selectedCustomChannels,
      selectedSyncedCustomChannels,
    } = this.state;

    // If an organization is selected, show its synced channels; otherwise, show an empty list.
    const syncedChannels = selectedOrg ? syncedCustomChannels[selectedOrg.id] || [] : [];

    return (
      <div className="container mt-4">
        <h3>Sync Organizations to Peripheral Channels</h3>
        <div className="row">
          {/* Organizations Table */}
          <div className="col-md-4">
            <h5>Organizations</h5>
            <Table data={availableOrgs} identifier={(row: Org) => row.id} selectable={false}>
              <Column
                columnKey="label"
                comparator={Utils.sortByText}
                header={"Name"}
                cell={(row: Org) => (
                  <>
                    <div onClick={() => this.handleOrgSelect(row)}>{row.label}</div>
                  </>
                )}
              />
            </Table>
          </div>

          {/* Channels Dual-List Section */}
          <div className="col-md-8">
            <h5>
              {selectedOrg ? `Organization: ${selectedOrg.label} (ID: ${selectedOrg.id})` : "No Organization Selected"}
            </h5>
            <div className="row">
              {/* Synced Channels Table */}
              <div className="col-md-5">
                <h6>Synced Channels</h6>
                <Table
                  data={syncedChannels}
                  identifier={(row: Channel) => row.id}
                  selectable={true}
                  onSelect={this.handleSyncedCustomChannelSelect}
                >
                  <Column
                    columnKey="name"
                    comparator={Utils.sortByText}
                    header={"Name"}
                    cell={(row: Channel) => <span>{row.name}</span>}
                  />
                  <Column
                    columnKey="label"
                    comparator={Utils.sortByText}
                    header={"Label"}
                    cell={(row: Channel) => <span>{row.label}</span>}
                  />
                </Table>
              </div>

              {/* Buttons to move channels between lists */}
              <div className="col-md-2 d-flex flex-column justify-content-center align-items-center">
                <button
                  type="button"
                  className="btn btn-primary mb-2"
                  onClick={this.handleAddChannels}
                  disabled={!selectedOrg || selectedCustomChannels.length === 0}
                >
                  Add &gt;&gt;
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={this.handleRemoveChannels}
                  disabled={!selectedOrg || selectedSyncedCustomChannels.length === 0}
                >
                  &lt;&lt; Remove
                </button>
              </div>

              {/* Available Channels Table */}
              <div className="col-md-5">
                <h6>Available Channels</h6>
                <Table
                  data={availableCustomChannels}
                  identifier={(row: Channel) => row.id}
                  selectable={true}
                  onSelect={this.handleCustomChannelSelect}
                >
                  <Column
                    columnKey="name"
                    comparator={Utils.sortByText}
                    header={"Name"}
                    cell={(row: Channel) => <span>{row.name}</span>}
                  />
                  <Column
                    columnKey="label"
                    comparator={Utils.sortByText}
                    header={"Label"}
                    cell={(row: Channel) => <span>{row.label}</span>}
                  />
                </Table>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
  }
