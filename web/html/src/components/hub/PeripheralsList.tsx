import * as React from "react";

import { LinkButton } from "components/buttons";
import { DeregisterServer, IssRole, PeripheralListData } from "components/hub";
import { LargeTextAttachment } from "components/large-text-attachment";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table, TableRef } from "components/table/Table";

import { Utils } from "utils/functions";

type Props = {};

export class PeripheralsList extends React.Component<Props> {
  private tableRef: React.RefObject<TableRef>;

  public constructor(props: Props) {
    super(props);

    this.tableRef = React.createRef();
  }

  public render(): React.ReactNode {
    const componentContent = (
      <Table
        ref={this.tableRef}
        data="/rhn/manager/api/admin/hub/peripherals"
        identifier={(row: PeripheralListData) => row.id}
        initialSortColumnKey="fqdn"
        defaultSearchField="fqdn"
        searchField={<SearchField filter={this.searchData} placeholder={t("Filter by FQDN")} />}
      >
        <Column
          columnKey="fqdn"
          comparator={Utils.sortByText}
          header={t("Peripheral FQDN")}
          cell={(row) => this.renderFqdnLink(row)}
        />
        <Column
          columnKey="nChannelsSync"
          header={t("N. of synced channels")}
          cell={(row: PeripheralListData) => row.nSyncedChannels}
        />
        <Column
          columnKey="nOrgs"
          header={t("N. of synced organizations")}
          cell={(row: PeripheralListData) => row.nSyncedOrgs}
        />
        <Column
          columnKey="download"
          header={t("Download Root CA")}
          cell={(row: PeripheralListData) => this.renderDownloadRootCA(row)}
        />
        <Column
          columnKey="remove"
          header={t("Delete")}
          cell={(row: PeripheralListData) => this.renderDeregister(row)}
        />
      </Table>
    );

    return componentContent;
  }

  private renderFqdnLink(row: PeripheralListData): React.ReactNode {
    return (
      <LinkButton
        className="btn-link"
        text={row.fqdn}
        title={t("View details of peripheral {fqdn}", row)}
        href={`/rhn/manager/admin/hub/peripherals/${row.id}`}
      />
    );
  }

  private renderDownloadRootCA(row: PeripheralListData): React.ReactNode {
    return (
      <LargeTextAttachment value={row.rootCA} filename={row.fqdn + "_CA.pem"} editable={false} hideMessage={true} />
    );
  }

  private renderDeregister(row: PeripheralListData): React.ReactNode {
    return (
      <DeregisterServer
        id={row.id}
        fqdn={row.fqdn}
        role={IssRole.Peripheral}
        onDeregistered={() => this.onServerDeregistered()}
      />
    );
  }

  private onServerDeregistered(): void {
    this.tableRef.current?.refresh();
  }

  private searchData(row: PeripheralListData, criteria: string | undefined): boolean {
    if (criteria !== undefined) {
      return row.fqdn.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());
    }
    return true;
  }
}
