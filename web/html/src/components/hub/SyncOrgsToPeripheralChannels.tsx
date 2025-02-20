import * as React from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { showSuccessToastr } from "components/toastr";

import Network from "utils/network";

// Types for organization and channel.
export type Org = {
  orgId: number;
  orgName: string;
};

//TODO: logic for parent and clone channels
// if a parent is selected for sync, sync all child channels too
// if a clone of a vendor is selected for sync, sync father Vendor channel too
export type Channel = {
  channelId: number;
  channelName: string;
  channelLabel: string;
  channelArch: string;
  // null channelOrg means is a Vendor Channel
  channelOrg: Org | null;
};

export type SyncPeripheralsProps = {
  availableOrgs: Org[];
  availableCustomChannels: Channel[];
  availableVendorChannels: Channel[];
  syncedCustomChannels: Channel[];
  syncedVendorChannels: Channel[];
};

type State = {
  selectedOrg: Org | null;
  syncedChannels: Channel[];
  availableOrgs: Org[];
  availableChannels: Channel[];
  syncModalOpen: boolean;
  modalSelectedChannels: Channel[];
};

type SyncChannelRequest = {
  channel: string;
};

type RemoveSyncChannelRequest = {
  channel: string;
};

export class SyncOrgsToPeripheralChannel extends React.Component<SyncPeripheralsProps, State> {
  constructor(props: SyncPeripheralsProps) {
    super(props);
    this.state = {
      selectedOrg: null,
      syncedChannels: props.syncedCustomChannels.concat(props.syncedVendorChannels),
      availableOrgs: props.availableOrgs,
      availableChannels: props.availableCustomChannels.concat(props.availableVendorChannels),
      syncModalOpen: false,
      modalSelectedChannels: [],
    };
  }

  onChannelSyncConfirm = (channels: Channel[]) => {
    let newChannels = this.state.syncedChannels.concat(channels);
    this.setState({ syncedChannels: newChannels });
    /*
    const request = ""
    Network.post("/rhn/manager/api/admin/hub/peripheral/:id/sync-channels", request)
      .catch((xhr) => Network.showResponseErrorToastr(xhr))
      .then((response) => {
        // On successfull sync to peripheral
        
      })
      .finally(() => {
        showSuccessToastr(t("Channels synced correctly to peripheral!"));
      });*/
  };

  onChannelToSyncSelect = (channels: Channel[]) => {
    this.setState({ modalSelectedChannels: channels });
  };

  onChannelSyncModalOpen = () => this.setState({ selectedOrg: null, modalSelectedChannels: [], syncModalOpen: true });

  onChannelSyncModalClose = () => this.setState({ selectedOrg: null, modalSelectedChannels: [], syncModalOpen: false });

  render() {
    const { syncedChannels, availableChannels } = this.state;

    const searchData = (row, criteria) => {
      const keysToSearch = ["name"];
      if (criteria) {
        const needle = criteria.toLocaleLowerCase();
        return keysToSearch.map((key) => row[key]).some((item) => item.toLocaleLowerCase().includes(needle));
      }
      return true;
    };

    const syncedChannelsTable = (
      <>
        <span>
          <h3>{t("Synced Channels")}</h3>
        </span>
        <Table
          data={syncedChannels}
          identifier={(row: Channel) => row.channelId}
          selectable={false}
          initialSortColumnKey="name"
          searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
        >
          <Column columnKey="name" header={t("Name")} cell={(row: Channel) => <span>{row.channelName}</span>} />
          <Column columnKey="label" header={t("Label")} cell={(row: Channel) => <span>{row.channelLabel}</span>} />
          <Column columnKey="arch" header={t("Arch")} cell={(row: Channel) => <span>{row.channelArch}</span>} />
          <Column
            columnKey="orgName"
            header={t("Org")}
            cell={(row: Channel) => <span>{row.channelOrg ? row.channelOrg.orgName : "SUSE"}</span>}
          />
          <Column columnKey="remove" header={t("Remove")} cell={(row: Channel) => <i className="fa fa-trash"></i>} />
        </Table>
      </>
    );

    const modalContent = (
      <>
        <span>
          <h4>Select an organizzation from the Peripheral to Sync your channels to:</h4>
        </span>
        <span>
          <h5>(Vendor channels are automatically synced to SUSE Organization)</h5>
        </span>
        <span>
          <h3>{t("Available Channels")}</h3>
        </span>
        <Table
          data={availableChannels}
          identifier={(row: Channel) => row.channelId}
          selectable={true}
          onSelect={this.onChannelToSyncSelect}
          selectedItems={this.state.modalSelectedChannels}
          initialSortColumnKey="name"
          searchField={<SearchField filter={searchData} placeholder={t("Filter by Name")} />}
        >
          <Column columnKey="name" header={t("Name")} cell={(row: Channel) => <span>{row.channelName}</span>} />
          <Column columnKey="label" header={t("Label")} cell={(row: Channel) => <span>{row.channelLabel}</span>} />
          <Column columnKey="arch" header={t("Arch")} cell={(row: Channel) => <span>{row.channelArch}</span>} />
          <Column
            columnKey="orgName"
            header={t("Org")}
            cell={(row: Channel) => <span>{row.channelOrg ? row.channelOrg.orgName : "SUSE"}</span>}
          />
        </Table>
      </>
    );

    const modalFooter = (
      <>
        <div className="col-lg-6">
          <div className="pull-right btn-group">
            <Button
              id="sync-modal-confirm"
              className="btn-primary"
              text={t("Confirm")}
              handler={this.onChannelSyncConfirm}
            />
            <Button
              id="sync-modal-cancel"
              className="btn-danger"
              text="Cancel"
              handler={this.onChannelSyncModalClose}
            />
          </div>
        </div>
      </>
    );
    return (
      <div className="container mt-4">
        <h3>{t("Sync Channels from Hub to Peripheral")}</h3>
        <div className="synced-channels mb-3">{syncedChannelsTable}</div>
        {/* Modal Button to Open the Channel Selection Modal */}
        <div className="text-center mb-4">
          <Button
            className="btn-primary"
            title={t("Add Channels")}
            text={t("Add Channels")}
            handler={this.onChannelSyncModalOpen}
          />
        </div>
        {/* Modal Dialog with Available Channels Table */}
        <Dialog
          id="sync-channel-modal"
          title={t("Add Channel to Sync")}
          content={modalContent}
          isOpen={this.state.syncModalOpen}
          footer={modalFooter}
          onClose={this.onChannelSyncModalClose}
        />
      </div>
    );
  }
}
