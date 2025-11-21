import { type RefObject, Component, createRef } from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { TopPanel } from "components/panels";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table, TableRef } from "components/table/Table";
import { showSuccessToastr, showWarningToastr } from "components/toastr";

import Network from "utils/network";

import { ChannelSelectorTable } from "./ChannelSelectorTable";
import { Channel, ChannelSyncProps, Org } from "./types";

type SyncPeripheralsProps = {
  peripheralId: number;
  peripheralFqdn: string;
  availableOrgs: Org[];
  channels: Channel[];
  mandatoryMap: Map<number, number[]>;
  reversedMandatoryMap: Map<number, number[]>;
};

type State = {
  peripheralId: number;
  peripheralFqdn: string;
  channels: Channel[];
  availableOrgs: Org[];
  syncModalOpen: boolean;
  channelsToAdd: Channel[];
  channelsToRemove: Channel[];
  loading: boolean;
};

export class SyncOrgsToPeripheralChannel extends Component<SyncPeripheralsProps, State> {
  private tableRef: RefObject<TableRef> = createRef();

  constructor(props: SyncPeripheralsProps) {
    super(props);

    props.channels.forEach((channel) => {
      if (!channel) return;
      if (channel.peripheralOrg !== null) {
        channel.strictOrg = true;
      }
    });

    this.state = {
      peripheralId: props.peripheralId,
      peripheralFqdn: props.peripheralFqdn,
      channels: props.channels,
      availableOrgs: props.availableOrgs,
      syncModalOpen: false,
      channelsToAdd: [],
      channelsToRemove: [],
      loading: false,
    };
  }

  componentDidUpdate(prevProps: SyncPeripheralsProps) {
    if (prevProps.channels !== this.props.channels) {
      this.setStateFromApiProps(this.props);
    }
  }

  private setStateFromApiProps(props: SyncPeripheralsProps) {
    this.setState({
      availableOrgs: props.availableOrgs,
      channels: props.channels,
      loading: false,
      // Reset change tracking when API data changes
      channelsToAdd: [],
      channelsToRemove: [],
    });
    this.tableRef.current?.refresh();
  }

  private openCloseModalState(isOpen: boolean) {
    this.setState({ syncModalOpen: isOpen });
  }

  handleChannelSelect = (channels: Channel[], selected: boolean) => {
    const channelsToAddSet = new Set<Channel>(this.state.channelsToAdd);
    const channelsToRemoveSet = new Set<Channel>(this.state.channelsToRemove);

    channels.forEach((channel) => {
      if (selected && !channel.synced) {
        // User wants to sync the channel, and it's not synced yet: add it to channelsToAdd
        channelsToAddSet.add(channel);
      } else if (selected && channel.synced) {
        // User wants to sync the channel, but it's already synced: user changed their mind, remove it from channelsToRemove
        channelsToRemoveSet.delete(channel);
      } else if (!selected && channel.synced) {
        // User wants to stop syncing for the channel and it's currently synced: add it to channelsToRemove
        channelsToRemoveSet.add(channel);
      } else if (!selected && !channel.synced) {
        // User wants to stop syncing for the channel but it's not synced: user changed their mind, remove it from channelsToAdd
        channelsToAddSet.delete(channel);
      }
    });

    this.setState({
      channelsToAdd: Array.from(channelsToAddSet),
      channelsToRemove: Array.from(channelsToRemoveSet),
    });
  };

  onChannelSyncConfirm = () => {
    const { peripheralId, channelsToAdd, channelsToRemove } = this.state;
    // Check if there's anything to do
    if (channelsToAdd.length === 0 && channelsToRemove.length === 0) {
      showWarningToastr(t("No changes to apply"));
      return;
    }
    const channelsToAddByOrg: { orgId: number | null; channelLabels: string[] }[] = [];
    const orgGroups: Record<string, Channel[]> = {};
    // First, group channel IDs by orgId
    channelsToAdd.forEach((channel) => {
      if (!channel) return;
      // For vendor channels, use null as orgId
      const orgId = channel.peripheralOrg ? channel.peripheralOrg.orgId : null;
      const key = orgId === null ? "null" : orgId.toString();
      if (!orgGroups[key]) {
        orgGroups[key] = [];
      }
      orgGroups[key].push(channel);
    });

    // Then, convert each group to the required format
    Object.entries(orgGroups).forEach(([orgKey, channels]) => {
      const orgId = orgKey === "null" ? null : parseInt(orgKey, 10);
      const channelLabels = channels.map((channel) => channel.label);
      if (channelLabels.length > 0) {
        channelsToAddByOrg.push({
          orgId,
          channelLabels,
        });
      }
    });

    const channelsToRemoveLabels = channelsToRemove.map((channel) => channel.label);

    const payload = {
      channelsToAdd: channelsToAddByOrg,
      channelsToRemove: channelsToRemoveLabels,
    };

    this.setState({ loading: true });
    const endpoint = `/rhn/manager/api/admin/hub/peripherals/${peripheralId}/sync-channels`;

    Network.post(endpoint, payload)
      .then(() => {
        showSuccessToastr(t("Channels synced correctly to peripheral!"));
        // Refresh the data after successful sync
        return Network.get(endpoint);
      })
      .then((response) => {
        const channelSync: ChannelSyncProps = JSON.parse(response);
        channelSync.channels.forEach((channel) => {
          if (channel.peripheralOrg !== null) {
            channel.strictOrg = true;
          }
        });

        const newProps = {
          peripheralId: this.props.peripheralId,
          peripheralFqdn: this.props.peripheralFqdn,
          availableOrgs: channelSync.peripheralOrgs,
          channels: channelSync.channels,
          mandatoryMap: this.props.mandatoryMap,
          reversedMandatoryMap: this.props.reversedMandatoryMap,
        };
        this.setStateFromApiProps(newProps);
        this.openCloseModalState(false);
      })
      .catch((error) => {
        Network.showResponseErrorToastr(error);
        this.setState({ loading: false });
        this.openCloseModalState(false);
      });
  };

  onChannelSyncModalOpen = () => {
    const { channelsToAdd, channelsToRemove } = this.state;
    // Only open the modal if there are changes to apply
    if (channelsToAdd.length > 0 || channelsToRemove.length > 0) {
      this.openCloseModalState(true);
    } else {
      showWarningToastr(t("Please select at least one channel to add or remove from sync"));
    }
  };

  onChannelSyncModalClose = () => {
    this.openCloseModalState(false);
  };

  render() {
    const { channels, syncModalOpen, availableOrgs, loading, channelsToAdd, channelsToRemove } = this.state;

    const searchData = (row, criteria) => {
      const keysToSearch = ["channelName"];
      if (criteria) {
        const needle = criteria.toLocaleLowerCase();
        return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
      }
      return true;
    };

    const renderChannelName = (channel: Channel): JSX.Element => <span>{channel.name}</span>;
    const renderChannelLabel = (channel: Channel): JSX.Element => <span>{channel.label}</span>;
    const renderChannelArchitecture = (channel: Channel): JSX.Element => <span>{channel.architecture}</span>;
    const renderChannelOrganization = (channel: Channel): JSX.Element => {
      // For vendor channels (peripheralOrgs empty), always show "Vendor"
      if (channel.hubOrg === null) {
        return <span>Vendor</span>;
      }
      if (channel.peripheralOrg !== undefined || channel.peripheralOrg !== null) {
        return <span>{channel.peripheralOrg ? channel.peripheralOrg.orgName : "Unknown"}</span>;
      } else {
        // If no org is explicitly selected for syncing, show "Not set"
        return <span className="text-warning">Not set</span>;
      }
    };

    const channelsToAddTable = (
      <>
        <h4 className="mt-4">{t("Channels to Add")}</h4>
        {channelsToAdd.length > 0 ? (
          <>
            <Table
              data={channelsToAdd}
              identifier={(row: Channel) => row.id}
              selectable={false}
              initialSortColumnKey="channelName"
              searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
            >
              <Column columnKey="name" header={t("Name")} cell={renderChannelName} />
              <Column columnKey="label" header={t("Label")} cell={renderChannelLabel} />
              <Column columnKey="architecture" header={t("Arch")} cell={renderChannelArchitecture} />
              <Column columnKey="organization" header={t("Sync Org")} cell={renderChannelOrganization} />
            </Table>
            {/* Display a warning if any non-vendor channel doesn't have an org mapping */}
            {channelsToAdd.some(
              (channel) =>
                (channel.hubOrg && channel.peripheralOrg === undefined) ||
                (channel.hubOrg && channel.peripheralOrg === null)
            ) && (
              <div className="alert alert-warning">
                <span className="fa fa-exclamation-triangle"></span>{" "}
                {t(
                  "Some custom channels do not have a sync organization selected. Please select organizations before confirming."
                )}
              </div>
            )}
          </>
        ) : (
          <p>{t("No channels selected to add")}</p>
        )}
      </>
    );
    // Table for channels to remove in the modal
    const channelsToRemoveTable = (
      <>
        <h4 className="mt-4">{t("Channels to Remove")}</h4>
        {channelsToRemove.length > 0 ? (
          <Table
            data={channelsToRemove}
            identifier={(row: Channel) => row.id}
            selectable={false}
            initialSortColumnKey="name"
            searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
          >
            <Column columnKey="name" header={t("Name")} cell={renderChannelName} />
            <Column columnKey="label" header={t("Label")} cell={renderChannelLabel} />
            <Column columnKey="architecture" header={t("Arch")} cell={renderChannelArchitecture} />
            <Column columnKey="organization" header={t("Sync Org")} cell={renderChannelOrganization} />
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
            id="sync-modal-cancel"
            className="btn-default"
            text={t("Cancel")}
            disabled={loading}
            handler={this.onChannelSyncModalClose}
          />
          <Button
            id="sync-modal-confirm"
            className="btn-primary"
            text={t("Confirm")}
            disabled={loading || (channelsToAdd.length === 0 && channelsToRemove.length === 0)}
            handler={this.onChannelSyncConfirm}
          />
        </div>
      </div>
    );

    return (
      <TopPanel
        title={t("{peripheralFqdn} - Peripheral Sync Channels", this.props)}
        icon="fa-cogs"
        helpUrl="specialized-guides/large-deployments/hub-online-sync.html#_synchronize_channels_from_hub_to_peripheral_server"
      >
        <SectionToolbar>
          <div className="selector-button-wrapper">
            <div className="btn-group pull-left">
              <Button
                className="btn-default"
                icon="fa-chevron-left"
                text={t("Back to details")}
                handler={() =>
                  window.pageRenderers?.spaengine?.navigate?.(
                    `/rhn/manager/admin/hub/peripherals/${this.props.peripheralId}`
                  )
                }
              />
            </div>
          </div>
          <div className="action-button-wrapper">
            <div className="btn-group pull-right">
              <Button
                className="btn-primary"
                title={t("Apply Changes")}
                text={t("Apply Changes")}
                disabled={loading || (channelsToAdd.length === 0 && channelsToRemove.length === 0)}
                handler={this.onChannelSyncModalOpen}
              />
            </div>
          </div>
        </SectionToolbar>
        <div className="container mt-4">
          <h3>{t("Sync Channels from Hub to Peripheral")}</h3>
          <ChannelSelectorTable
            channels={channels}
            allChannelIds={async () => [101, 102, 103, 104, 105]}
            availableOrgs={availableOrgs}
            requiresMap={this.props.mandatoryMap}
            requiredByMap={this.props.reversedMandatoryMap}
            onChannelSyncChange={this.handleChannelSelect}
          />
          <Dialog
            id="sync-channel-modal"
            title={t("Confirm Channel Synchronization Changes")}
            content={modalContent}
            isOpen={syncModalOpen}
            footer={modalFooter}
            onClose={this.onChannelSyncModalClose}
          />
        </div>
      </TopPanel>
    );
  }
}

export default SyncOrgsToPeripheralChannel;
