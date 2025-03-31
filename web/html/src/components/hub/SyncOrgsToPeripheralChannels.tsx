import * as React from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table, TableRef } from "components/table/Table";
import { showSuccessToastr, showWarningToastr } from "components/toastr";

import Network from "utils/network";

import ChannelHierarchicalTable from "./HierarchicalChannelsTable";
import { Channel, FlatChannel, Org } from "./types";

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
  channelsToAdd: number[];
  channelsToRemove: number[];
  loading: boolean;
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
      channelsToAdd: [],
      channelsToRemove: [],
      loading: false,
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

    this.setState({
      syncedChannels,
      availableOrgs: props.availableOrgs,
      availableChannels,
      loading: false,
      // Reset change tracking when API data changes
      channelsToAdd: [],
      channelsToRemove: [],
    });
  }

  private openCloseModalState(isOpen: boolean) {
    this.setState({ syncModalOpen: isOpen });
  }

  handleChannelSelect = (channelId: number, checked: boolean) => {
    const { channelsToAdd, channelsToRemove, syncedChannels } = this.state;
    // Check if the channel is already synced (exists in syncedChannels)
    const isChannelSynced = syncedChannels.some((channel) => channel.channelId === channelId);
    if (checked) {
      // User is checking the channel (wants to sync it)
      if (isChannelSynced) {
        // If it's already synced but was in channelsToRemove (user changed their mind),
        // remove it from channelsToRemove
        if (channelsToRemove.includes(channelId)) {
          this.setState({
            channelsToRemove: channelsToRemove.filter((id) => id !== channelId),
          });
        }
      } else {
        // If it's not synced yet, add it to channelsToAdd (unless it's already there)
        if (!channelsToAdd.includes(channelId)) {
          this.setState({
            channelsToAdd: [...channelsToAdd, channelId],
          });
        }
      }
    } else {
      // User is unchecking the channel (wants to unsync it)
      if (isChannelSynced) {
        // If it's currently synced, add it to channelsToRemove (unless it's already there)
        if (!channelsToRemove.includes(channelId)) {
          this.setState({
            channelsToRemove: [...channelsToRemove, channelId],
          });
        }
      } else {
        // If it's not synced but was in channelsToAdd (user changed their mind),
        // remove it from channelsToAdd
        if (channelsToAdd.includes(channelId)) {
          this.setState({
            channelsToAdd: channelsToAdd.filter((id) => id !== channelId),
          });
        }
      }
    }
  };

  handleOrgSelect = (channelId: number, orgId?: number) => {
    // Handle Org selection here
    // Put into a record for channelid and orgid
  };

  onChannelSyncConfirm = () => {
    const { peripheralId, channelsToAdd, channelsToRemove } = this.state;
    // Get channels to add based on channelsToAdd IDs
    const allChannels = [...this.state.syncedChannels, ...this.state.availableChannels];
    const channelsToAddLabels = channelsToAdd
      .map((id) => {
        const channel = allChannels.find((c) => c.channelId === id);
        return channel ? channel.channelLabel : null;
      })
      .filter(Boolean);
    // Get channels to remove based on channelsToRemove IDs
    const channelsToRemoveLabels = channelsToRemove
      .map((id) => {
        const channel = allChannels.find((c) => c.channelId === id);
        return channel ? channel.channelLabel : null;
      })
      .filter(Boolean);
    // If nothing to sync or unsync, show warning
    if (channelsToAddLabels.length === 0 && channelsToRemoveLabels.length === 0) {
      showWarningToastr(t("No changes to apply"));
      return;
    }

    // Prepare payload
    const payload = {
      // Include channels to add for sync
      channelsLabelsToAdd: channelsToAddLabels,
      // Include channels to remove from sync
      channelsLabelsToRemove: channelsToRemoveLabels,
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
    const { channelsToAdd, channelsToRemove } = this.state;
    // Only open the modal if there are changes to apply
    if (channelsToAdd.length > 0 || channelsToRemove.length > 0) {
      this.openCloseModalState(true);
    } else {
      // Show a message that no changes are selected
      showWarningToastr(t("Please select at least one channel to add or remove from sync"));
    }
  };

  onChannelSyncModalClose = () => {
    this.openCloseModalState(false);
  };

  render() {
    const {
      syncedChannels,
      availableChannels,
      syncModalOpen,
      availableOrgs,
      loading,
      channelsToAdd,
      channelsToRemove,
    } = this.state;

    // Combine all channels for the hierarchical table
    const allChannels = [...availableChannels, ...syncedChannels];

    // Find channels to add and remove for display in the modal
    const channelsToAddData = allChannels.filter((channel) => channelsToAdd.includes(channel.channelId));
    const channelsToRemoveData = allChannels.filter((channel) => channelsToRemove.includes(channel.channelId));

    const searchData = (row, criteria) => {
      const keysToSearch = ["channelName", "channelLabel"];
      if (criteria) {
        const needle = criteria.toLocaleLowerCase();
        return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
      }
      return true;
    };

    const renderChannelName = (channel: Channel): JSX.Element => <span>{channel.channelName}</span>;
    const renderChannelLabel = (channel: Channel): JSX.Element => <span>{channel.channelLabel}</span>;
    const renderChannelArch = (channel: Channel): JSX.Element => <span>{channel.channelArch}</span>;
    const renderChannelHubOrg = (channel: Channel): JSX.Element => (
      <span>{channel.channelOrg ? channel.channelOrg.orgName : "SUSE"}</span>
    );
    const renderChannelSyncOrg = (channel: Channel): JSX.Element => (
      // Check if the channel has a selected org and is not a vendor channel
      <span>{channel.channelOrg ? channel.channelOrg.orgName : "SUSE"}</span>
    );

    // Table for channels to add in the modal
    const channelsToAddTable = (
      <>
        <h4 className="mt-4">{t("Channels to Add")}</h4>
        {channelsToAddData.length > 0 ? (
          <Table
            data={channelsToAddData}
            identifier={(row: FlatChannel) => row.channelId}
            selectable={false}
            initialSortColumnKey="channelName"
            searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
          >
            <Column columnKey="channelName" header={t("Name")} cell={renderChannelName} />
            <Column columnKey="channelLabel" header={t("Label")} cell={renderChannelLabel} />
            <Column columnKey="channelArch" header={t("Arch")} cell={renderChannelArch} />
            <Column columnKey="orgName" header={t("Hub Org")} cell={renderChannelHubOrg} />
            <Column columnKey="orgName" header={t("Sync Org")} cell={renderChannelSyncOrg} />
          </Table>
        ) : (
          <p>{t("No channels selected to add")}</p>
        )}
      </>
    );

    // Table for channels to remove in the modal
    const channelsToRemoveTable = (
      <>
        <h4 className="mt-4">{t("Channels to Remove")}</h4>
        {channelsToRemoveData.length > 0 ? (
          <Table
            data={channelsToRemoveData}
            identifier={(row: FlatChannel) => row.channelId}
            selectable={false}
            initialSortColumnKey="channelName"
            searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
          >
            <Column columnKey="channelName" header={t("Name")} cell={renderChannelName} />
            <Column columnKey="channelLabel" header={t("Label")} cell={renderChannelLabel} />
            <Column columnKey="channelArch" header={t("Arch")} cell={renderChannelArch} />
            <Column columnKey="orgName" header={t("Hub Org")} cell={renderChannelHubOrg} />
            <Column columnKey="orgName" header={t("Sync Org")} cell={renderChannelSyncOrg} />
          </Table>
        ) : (
          <p>{t("No channels selected to remove")}</p>
        )}
      </>
    );

    const modalContent = (
      <>
        <h3 className="mt-4">{t("Channel Sync Changes")}</h3>
        <p>{t("You are about to make the following changes:")}</p>
        {channelsToAddTable}
        {channelsToRemoveTable}
      </>
    );

    const modalFooter = (
      <div className="col-lg-12">
        <div className="pull-right btn-group">
          <Button
            id="sync-modal-confirm"
            className="btn-primary"
            text={t("Confirm")}
            disabled={loading || (channelsToAdd.length === 0 && channelsToRemove.length === 0)}
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
        <ChannelHierarchicalTable
          channels={allChannels}
          availableOrgs={availableOrgs}
          onChannelSelect={this.handleChannelSelect}
          onOrgSelect={this.handleOrgSelect}
          loading={loading}
        />
        <div className="text-center mt-4 mb-4">
          <Button
            className="btn-primary"
            title={t("Apply Changes")}
            text={t("Apply Changes")}
            disabled={loading || (channelsToAdd.length === 0 && channelsToRemove.length === 0)}
            handler={this.onChannelSyncModalOpen}
          />
        </div>
        <Dialog
          id="sync-channel-modal"
          title={t("Confirm Channel Synchronization Changes")}
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
