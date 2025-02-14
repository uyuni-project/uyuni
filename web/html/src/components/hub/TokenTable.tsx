import * as React from "react";

import { pageSize } from "core/user-preferences";

import { FromNow } from "components/datetime";
import { Column } from "components/table/Column";
import { Table, TableRef } from "components/table/Table";

import { localizedMoment } from "utils/datetime";

import { AccessToken } from "./types";

type Props = {};

type State = {
  selectedRow: AccessToken | undefined;
  confirmValidityDialog: boolean;
  confirmDeleteDialog: boolean;
};

export class TokenTable extends React.Component<Props, State> {
  private tableRef: React.RefObject<TableRef>;

  public constructor(props: Props) {
    super(props);

    this.tableRef = React.createRef();
    this.state = {
      selectedRow: undefined,
      confirmValidityDialog: false,
      confirmDeleteDialog: false,
    };
  }

  public render(): React.ReactNode {
    return (
      <Table
        ref={this.tableRef}
        selectable={false}
        data="/rhn/manager/api/admin/hub/access-tokens"
        identifier={(row: AccessToken) => row.id}
        initialItemsPerPage={pageSize}
        emptyText={t("No tokens available")}
        initialSortColumnKey="executionTime"
      >
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="attestation"
          header={t("Token type")}
          cell={(row: AccessToken) => row.localizedType}
        />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="attestation"
          header={t("Server")}
          cell={(row: AccessToken) => row.serverFqdn}
        />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="valid"
          header={t("Valid")}
          cell={(row: AccessToken) => (row.valid ? t("Yes") : t("No"))}
        />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="expirationTime"
          header={t("Expired")}
          cell={(row: AccessToken) => this.renderExpiration(row.expirationDate)}
        />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="creationDate"
          header={t("Created")}
          cell={(row: AccessToken) => <FromNow value={row.creationDate} />}
        />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="modificationDate"
          header={t("Last Modified")}
          cell={(row: AccessToken) => <FromNow value={row.modificationDate} />}
        />
      </Table>
    );
  }

  private renderExpiration(expirationTime: Date | null): React.ReactNode {
    if (expirationTime === null) {
      return t("No, never expires");
    }

    const isExpired = localizedMoment(expirationTime).isBefore(localizedMoment());
    const param = { expiration: (_str) => <FromNow value={expirationTime} /> };

    return (
      <span className={isExpired ? "text-danger" : ""}>
        {isExpired ? t("Yes, expired <expiration></expiration>") : t("No, expires <expiration></expiration>", param)}
      </span>
    );
  }
}
