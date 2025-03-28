import * as React from "react";

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
    let componentContent = (
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
          cell={(row) => this.renderDetailsLink(row)}
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
        <Column header={t("Download Root CA")} cell={(row: PeripheralListData) => this.renderDownloadRootCA(row)} />
        <Column
          columnKey="remove"
          header={t("Delete")}
          cell={(row: PeripheralListData) => this.renderDeregister(row)}
        />
      </Table>
    );

    return componentContent;
  }

  private renderDownloadRootCA(row: PeripheralListData): React.ReactNode {
    return (
      <LargeTextAttachment value={row.rootCA} filename={row.fqdn + "_CA.pem"} editable={false} hideMessage={true} />
    );
  }

  private renderDetailsLink(row: PeripheralListData): React.ReactNode {
    const detailsUrl = "/rhn/manager/api/admin/hub/peripherals/" + row.id;
    return <a href={detailsUrl}>{row.fqdn}</a>;
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
