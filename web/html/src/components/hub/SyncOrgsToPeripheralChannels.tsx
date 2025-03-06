import * as React from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table, TableRef } from "components/table/Table";
import { showSuccessToastr } from "components/toastr";

import Network from "utils/network";

import { HierarchicalChannelTable } from "./HierarchicalChannelTable";
import { Channel, Org } from "./types";

type SyncPeripheralsProps = {
  peripheralId: number;
  availableOrgs: Org[];
  availableCustomChannels: Channel[];
  availableVendorChannels: Channel[];
  syncedCustomChannels: Channel[];
  syncedVendorChannels: Channel[];
};

type State = {
  peripheralId: number;
  syncedChannels: Channel[];
  availableOrgs: Org[];
  availableChannels: Channel[];
  syncModalOpen: boolean;
  modalSelectedChannels: number[];
  selectedOrg: Org | null;
};

export class SyncOrgsToPeripheralChannel extends React.Component<SyncPeripheralsProps, State> {
  private tableRef: React.RefObject<TableRef> = React.createRef();

  constructor(props: SyncPeripheralsProps) {
    super(props);
    this.state = {
      peripheralId: props.peripheralId,
      syncedChannels: props.syncedCustomChannels.concat(props.syncedVendorChannels),
      availableOrgs: props.availableOrgs,
      availableChannels: props.availableCustomChannels.concat(props.availableVendorChannels),
      syncModalOpen: false,
      modalSelectedChannels: [],
      selectedOrg: null,
    };
  }

  private setStateFromApiProps(props: SyncPeripheralsProps) {
    this.setState({
      syncedChannels: props.syncedCustomChannels.concat(props.syncedVendorChannels),
      availableOrgs: props.availableOrgs,
      availableChannels: props.availableCustomChannels.concat(props.availableVendorChannels),
    });
  }

  private openCloseModalState(isOpen: boolean) {
    this.setState({ selectedOrg: null, modalSelectedChannels: [], syncModalOpen: isOpen });
  }

  onChannelSyncConfirm = (event) => {
    const { peripheralId, modalSelectedChannels } = this.state;
    const endpoint = `/rhn/manager/api/admin/hub/peripheral/${peripheralId}/channels`;

    const syncChannels = () =>
      Network.post(endpoint, modalSelectedChannels).then(() => {
        showSuccessToastr(t("Channels synced correctly to peripheral!"));
        return Network.get(endpoint);
      });
    syncChannels()
      .then((response: SyncPeripheralsProps) => {
        this.setStateFromApiProps(response);
      })
      .catch((error) => {
        Network.showResponseErrorToastr(error);
      });
  };

  onChannelToSyncSelect = (channelsIds: number[]) => {
    this.setState({ modalSelectedChannels: channelsIds });
  };

  onChannelSyncModalOpen = () => {
    this.openCloseModalState(true);
  };

  onChannelSyncModalClose = () => {
    this.openCloseModalState(false);
  };

  render() {
    const { syncedChannels, availableChannels, modalSelectedChannels, syncModalOpen } = this.state;

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
          ref={this.tableRef}
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
          <h4>Select an organization from the Peripheral to Sync your channels to: //TODO: SelectBox</h4>
        </span>
        <span>(Vendor channels are automatically synced to SUSE Organization)</span>
        <span>
          <h3>{t("Available Channels")}</h3>
        </span>
        <HierarchicalChannelTable channels={availableChannels} onSelectionChange={this.onChannelToSyncSelect} />
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
          isOpen={syncModalOpen}
          footer={modalFooter}
          onClose={this.onChannelSyncModalClose}
        />
      </div>
    );
  }
}
export default SyncOrgsToPeripheralChannel;
