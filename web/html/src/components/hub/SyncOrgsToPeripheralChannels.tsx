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

import ChannelHierarchicalTable from "./HierarchicalChannelsTable";
import { Channel, ChannelSyncProps, FlatChannel, Org } from "./types";

type SyncPeripheralsProps = {
  peripheralId: number;
  peripheralFqdn: string;
  availableOrgs: Org[];
  channels: Channel[];
};

type State = {
  peripheralId: number;
  peripheralFqdn: string;
  channels: FlatChannel[];
  availableOrgs: Org[];
  syncModalOpen: boolean;
  channelsToAdd: number[];
  channelsToRemove: number[];
  loading: boolean;
};

/**
 * Converts a hierarchical array of Channel objects to a flat array of FlatChannel objects.
 * The resulting array includes all channels and their children, with parent-child
 * relationships preserved through the childrenIds property.
 *
 * @param channels - An array of hierarchical Channel objects
 * @returns An array of FlatChannel objects
 */
function flattenChannels(channels: Channel[]): FlatChannel[] {
  const flatChannels: FlatChannel[] = [];
  /**
   * Process a channel and its children recursively, adding them to the flat array
   * @param channel - The current channel to process
   */
  const processChannel = (channel: Channel): void => {
    const childrenLabels = channel.children.map((child) => child.channelLabel);
    const flatChannel: FlatChannel = {
      channelId: channel.channelId,
      channelName: channel.channelName,
      channelLabel: channel.channelLabel,
      channelArch: channel.channelArch,
      channelOrg: channel.channelOrg,
      selectedPeripheralOrg: channel.selectedPeripheralOrg,
      parentChannelLabel: channel.parentChannelLabel,
      childrenLabels: childrenLabels,
      strictOrg: channel.strictOrg,
      synced: channel.synced,
    };
    flatChannels.push(flatChannel);
    channel.children.forEach((child) => processChannel(child));
  };
  channels.forEach((channel) => processChannel(channel));
  return flatChannels;
}

export class SyncOrgsToPeripheralChannel extends Component<SyncPeripheralsProps, State> {
  private tableRef: RefObject<TableRef> = createRef();

  constructor(props: SyncPeripheralsProps) {
    super(props);

    props.channels.forEach((channel) => {
      if (!channel) return;
      if (channel.selectedPeripheralOrg !== null) {
        channel.strictOrg = true;
      }
    });

    this.state = {
      peripheralId: props.peripheralId,
      peripheralFqdn: props.peripheralFqdn,
      channels: flattenChannels(props.channels),
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
      channels: flattenChannels(props.channels),
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

  handleChannelSelect = (channelId: number, checked: boolean) => {
    const { channelsToAdd, channelsToRemove, channels } = this.state;
    // Check if the channel is already synced (has synced flag true)
    const isChannelSynced = channels.some((channel) => channel.channelId === channelId && channel.synced);
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

  handleOrgSelect = (channelId: number, org?: Org) => {
    this.setState((prevState) => ({
      channels: prevState.channels.map((channel) => {
        if (channel.channelId !== channelId) {
          return channel;
        }

        return {
          ...channel,
          selectedPeripheralOrg: org ?? null,
        };
      }),
    }));
  };

  onChannelSyncConfirm = () => {
    const { peripheralId, channelsToAdd, channelsToRemove, channels } = this.state;
    // Check if there's anything to do
    if (channelsToAdd.length === 0 && channelsToRemove.length === 0) {
      showWarningToastr(t("No changes to apply"));
      return;
    }
    const channelsToAddByOrg: { orgId: number | null; channelLabels: string[] }[] = [];
    const orgGroups: Record<string, number[]> = {};
    // First, group channel IDs by orgId
    channelsToAdd.forEach((id) => {
      const channel = channels.find((c) => c.channelId === id);
      if (!channel) return;
      // For vendor channels, use null as orgId
      const orgId = channel.selectedPeripheralOrg ? channel.selectedPeripheralOrg.orgId : null;
      const key = orgId === null ? "null" : orgId.toString();
      if (!orgGroups[key]) {
        orgGroups[key] = [];
      }
      orgGroups[key].push(id);
    });
    // Then, convert each group to the required format
    Object.entries(orgGroups).forEach(([orgKey, channelIds]) => {
      const orgId = orgKey === "null" ? null : parseInt(orgKey, 10);
      const channelLabels = channelIds
        .map((id) => channels.find((c) => c.channelId === id)?.channelLabel)
        .filter(Boolean);
      if (channelLabels.length > 0) {
        channelsToAddByOrg.push({
          orgId,
          channelLabels,
        });
      }
    });

    const channelsToRemoveLabels = channelsToRemove
      .map((id) => channels.find((c) => c.channelId === id)?.channelLabel)
      .filter(Boolean);

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
          if (channel.selectedPeripheralOrg !== null) {
            channel.strictOrg = true;
          }
        });

        const newProps = {
          peripheralId: this.props.peripheralId,
          peripheralFqdn: this.props.peripheralFqdn,
          availableOrgs: channelSync.peripheralOrgs,
          channels: channelSync.channels,
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

    const channelsToAddData = channels.filter((channel) => channelsToAdd.includes(channel.channelId));
    const channelsToRemoveData = channels.filter((channel) => channelsToRemove.includes(channel.channelId));

    const searchData = (row, criteria) => {
      const keysToSearch = ["channelName"];
      if (criteria) {
        const needle = criteria.toLocaleLowerCase();
        return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
      }
      return true;
    };

    const renderChannelName = (channel: Channel): JSX.Element => <span>{channel.channelName}</span>;
    const renderChannelLabel = (channel: Channel): JSX.Element => <span>{channel.channelLabel}</span>;
    const renderChannelArch = (channel: Channel): JSX.Element => <span>{channel.channelArch}</span>;

    const renderChannelSyncOrg = (channel: Channel): JSX.Element => {
      // For vendor channels (peripheralOrgs empty), always show "Vendor"
      if (channel.channelOrg === null) {
        return <span>Vendor</span>;
      }
      if (channel.selectedPeripheralOrg !== undefined || channel.selectedPeripheralOrg !== null) {
        return <span>{channel.selectedPeripheralOrg ? channel.selectedPeripheralOrg.orgName : "Unknown"}</span>;
      } else {
        // If no org is explicitly selected for syncing, show "Not set"
        return <span className="text-warning">Not set</span>;
      }
    };

    const channelsToAddTable = (
      <>
        <h4 className="mt-4">{t("Channels to Add")}</h4>
        {channelsToAddData.length > 0 ? (
          <>
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
              <Column columnKey="orgName" header={t("Sync Org")} cell={renderChannelSyncOrg} />
            </Table>
            {/* Display a warning if any non-vendor channel doesn't have an org mapping */}
            {channelsToAddData.some(
              (channel) =>
                (channel.channelOrg && channel.selectedPeripheralOrg === undefined) ||
                (channel.channelOrg && channel.selectedPeripheralOrg === null)
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

    const markedChannels = channels.map((channel) => {
      const isCurrentlySynced = channel.synced;
      const isPendingAddition = !isCurrentlySynced && channelsToAdd.includes(channel.channelId);
      const isPendingRemoval = isCurrentlySynced && channelsToRemove.includes(channel.channelId);
      const isChecked = (isCurrentlySynced && !isPendingRemoval) || (!isCurrentlySynced && isPendingAddition);
      return {
        ...channel,
        isChecked,
        isPendingAddition,
        isPendingRemoval,
      };
    });

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
          <ChannelHierarchicalTable
            channels={markedChannels}
            availableOrgs={availableOrgs}
            onChannelSelect={this.handleChannelSelect}
            onOrgSelect={this.handleOrgSelect}
            loading={loading}
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
