import * as React from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { Form, Select } from "components/input";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table, TableRef } from "components/table/Table";
import { showSuccessToastr, showWarningToastr } from "components/toastr";

import Network from "utils/network";

import ChannelHierarchicalTable from "./HierarchicalChannelTable";
import { FlatChannel, Org } from "./types";

type SyncPeripheralsProps = {
  peripheralId: number;
  availableOrgs: Org[];
  availableCustomChannels: FlatChannel[];
  availableVendorChannels: FlatChannel[];
  syncedCustomChannels: FlatChannel[];
  syncedVendorChannels: FlatChannel[];
};

type State = {
  peripheralId: number;
  syncedChannels: FlatChannel[];
  availableOrgs: Org[];
  availableChannels: FlatChannel[];
  syncModalOpen: boolean;
  selectedChannels: FlatChannel[];
  selectedOrg: Org | null;
  loading: boolean;
  syncStatus: Record<number, boolean>;
};

export class SyncOrgsToPeripheralChannel extends React.Component<SyncPeripheralsProps, State> {
  private tableRef: React.RefObject<TableRef> = React.createRef();

  constructor(props: SyncPeripheralsProps) {
    super(props);
    // Get the synced and available channels
    const syncedChannels = [...props.syncedCustomChannels, ...props.syncedVendorChannels];
    const availableChannels = [...props.availableCustomChannels, ...props.availableVendorChannels];
    // Initialize sync status for each channel
    const syncStatus: Record<number, boolean> = {};
    syncedChannels.forEach((channel) => {
      syncStatus[channel.channelId] = true;
    });
    availableChannels.forEach((channel) => {
      if (syncStatus[channel.channelId] === undefined) {
        syncStatus[channel.channelId] = false;
      }
    });

    this.state = {
      peripheralId: props.peripheralId,
      syncedChannels,
      availableOrgs: props.availableOrgs,
      availableChannels,
      syncModalOpen: false,
      selectedChannels: [],
      selectedOrg: null,
      loading: false,
      syncStatus,
    };
  }

  componentDidUpdate(prevProps: SyncPeripheralsProps) {
    if (
      prevProps.syncedCustomChannels !== this.props.syncedCustomChannels ||
      prevProps.syncedVendorChannels !== this.props.syncedVendorChannels ||
      prevProps.availableCustomChannels !== this.props.availableCustomChannels ||
      prevProps.availableVendorChannels !== this.props.availableVendorChannels
    ) {
      this.setStateFromApiProps(this.props);
    }
  }

  private setStateFromApiProps(props: SyncPeripheralsProps) {
    const syncedChannels = [...props.syncedCustomChannels, ...props.syncedVendorChannels];
    const availableChannels = [...props.availableCustomChannels, ...props.availableVendorChannels];
    // Update sync status for each channel
    const syncStatus: Record<number, boolean> = { ...this.state.syncStatus };
    syncedChannels.forEach((channel) => {
      syncStatus[channel.channelId] = true;
    });
    availableChannels.forEach((channel) => {
      if (syncStatus[channel.channelId] === undefined) {
        syncStatus[channel.channelId] = false;
      }
    });
    this.setState({
      syncedChannels,
      availableOrgs: props.availableOrgs,
      availableChannels,
      loading: false,
      syncStatus,
    });
  }

  private openCloseModalState(isOpen: boolean) {
    this.setState({ selectedOrg: null, syncModalOpen: isOpen });
  }

  handleSyncStatusChange = (channelId: number, synced: boolean) => {
    // When a channel's sync status changes, update selectedChannels accordingly
    const { syncStatus, selectedChannels, syncedChannels, availableChannels } = this.state;
    // Update the sync status
    const newSyncStatus = {
      ...syncStatus,
      [channelId]: synced,
    };
    // Find the channel from our available channels
    const allChannels = [...syncedChannels, ...availableChannels];
    const channel = allChannels.find((c) => c.channelId === channelId);
    if (!channel) return;
    let newSelectedChannels = [...selectedChannels];
    if (synced) {
      // If it was selected, add it to selectedChannels if not already there
      if (!newSelectedChannels.some((c) => c.channelId === channelId)) {
        newSelectedChannels.push(channel);
      }
    } else {
      // If it was unselected, remove it from selectedChannels
      newSelectedChannels = newSelectedChannels.filter((c) => c.channelId !== channelId);
    }
    this.setState({
      syncStatus: newSyncStatus,
      selectedChannels: newSelectedChannels,
    });
  };

  handleChannelSelect = (channel: FlatChannel) => {
    // Handle channel details view or other actions
    console.log(`Selected channel for details: ${channel.channelLabel}`);
  };

  onChannelSyncConfirm = () => {
    const { peripheralId, selectedChannels, selectedOrg } = this.state;

    if (!selectedOrg) {
      return;
    }
    // Get channel labels for the API call
    const channelsLabels: string[] = selectedChannels.map((channel) => channel.channelLabel);
    if (channelsLabels.length === 0) {
      return;
    }

    const payload = {
      channelsLabels: channelsLabels,
      selectedOrgId: selectedOrg.orgId,
    };

    this.setState({ loading: true });
    const endpoint = `/rhn/manager/api/admin/hub/peripherals/${peripheralId}/sync-channels`;
    Network.post(endpoint, payload)
      .then(() => {
        showSuccessToastr(t("Channels synced correctly to peripheral!"));
        // Refresh the data after successful sync
        return Network.get(endpoint);
      })
      .then((response: SyncPeripheralsProps) => {
        this.setStateFromApiProps(response);
        this.openCloseModalState(false);
      })
      .catch((error) => {
        Network.showResponseErrorToastr(error);
        this.setState({ loading: false });
      });
  };

  onChannelSyncModalOpen = () => {
    const { selectedChannels } = this.state;
    // Only open the modal if there are channels selected
    if (selectedChannels.length > 0) {
      this.openCloseModalState(true);
    } else {
      // Show a message that no channels are selected
      showWarningToastr(t("Please select at least one channel to sync"));
    }
  };

  onChannelSyncModalClose = () => {
    this.openCloseModalState(false);
  };

  render() {
    const { syncedChannels, availableChannels, selectedChannels, syncModalOpen, availableOrgs, loading, syncStatus } =
      this.state;

    // Combine all channels for the hierarchical table
    const allChannels = [...availableChannels, ...syncedChannels];

    const searchData = (row, criteria) => {
      const keysToSearch = ["channelName", "channelLabel"];
      if (criteria) {
        const needle = criteria.toLocaleLowerCase();
        return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
      }
      return true;
    };

    const renderChannelName = (channel: FlatChannel): JSX.Element => <span>{channel.channelName}</span>;

    const renderChannelLabel = (channel: FlatChannel): JSX.Element => <span>{channel.channelLabel}</span>;

    const renderChannelArch = (channel: FlatChannel): JSX.Element => <span>{channel.channelArch}</span>;

    const renderChannelOrg = (channel: FlatChannel): JSX.Element => (
      <span>{channel.channelOrg ? channel.channelOrg.orgName : "SUSE"}</span>
    );

    const selectedChannelsTable = (
      <>
        <Table
          data={selectedChannels}
          identifier={(row: FlatChannel) => row.channelId}
          selectable={false}
          initialSortColumnKey="channelName"
          ref={this.tableRef}
          searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
        >
          <Column columnKey="channelName" header={t("Name")} cell={renderChannelName} />
          <Column columnKey="channelLabel" header={t("Label")} cell={renderChannelLabel} />
          <Column columnKey="channelArch" header={t("Arch")} cell={renderChannelArch} />
          <Column columnKey="orgName" header={t("Hub Org")} cell={renderChannelOrg} />
        </Table>
      </>
    );

    const modalContent = (
      <>
        <div className="mb-4">
          <h4>{t("Select an organization from the Peripheral to Sync your channels to:")}</h4>
          <Form>
            <Select
              name="channel-orgs"
              placeholder={t("Select Organization")}
              options={availableOrgs.map((org) => ({
                value: org.orgId,
                label: org.orgName,
              }))}
              onChange={(_, selectedOrgId) => {
                const selectedOrg = availableOrgs.find((org) => org.orgId === selectedOrgId);
                this.setState({ selectedOrg: selectedOrg || null });
              }}
            />
          </Form>
          <small>{t("(Vendor channels are automatically synced to SUSE Organization)")}</small>
        </div>
        <h3 className="mt-4">{t("Selected Channels to Sync")}</h3>
        <p>
          {t("You have selected")} <strong>{selectedChannels.length}</strong> {t("channels to sync")}
        </p>
        {selectedChannelsTable}
      </>
    );

    const modalFooter = (
      <div className="col-lg-12">
        <div className="pull-right btn-group">
          <Button
            id="sync-modal-confirm"
            className="btn-primary"
            text={t("Confirm")}
            disabled={loading || !this.state.selectedOrg || selectedChannels.length === 0}
            handler={this.onChannelSyncConfirm}
          />
          <Button
            id="sync-modal-cancel"
            className="btn-default"
            text={t("Cancel")}
            disabled={loading}
            handler={this.onChannelSyncModalClose}
          />
        </div>
      </div>
    );

    return (
      <div className="container mt-4">
        <h3>{t("Sync Channels from Hub to Peripheral")}</h3>

        {/* Use our improved ChannelHierarchicalTable component */}
        <ChannelHierarchicalTable
          channels={allChannels}
          onSyncStatusChange={this.handleSyncStatusChange}
          onChannelSelect={this.handleChannelSelect}
          loading={loading}
          initialSyncStates={syncStatus}
        />

        {/* Button to Open the Channel Selection Modal */}
        <div className="text-center mt-4 mb-4">
          <Button
            className="btn-primary"
            title={t("Sync Channels")}
            text={t("Sync Channels")}
            disabled={loading || selectedChannels.length === 0}
            handler={this.onChannelSyncModalOpen}
          />
        </div>

        {/* Modal Dialog with Selected Channels Table */}
        <Dialog
          id="sync-channel-modal"
          title={t("Confirm Channel Synchronization")}
          content={modalContent}
          isOpen={syncModalOpen}
          footer={modalFooter}
          onClose={this.onChannelSyncModalClose}
        />
      </div>
    );
  }
}

export default SyncOrgsToPeripheralChannel;
