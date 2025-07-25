import * as React from "react";

import { pageSize } from "core/user-preferences";

import { Button } from "components/buttons";
import { FromNow } from "components/datetime";
import { DangerDialog } from "components/dialog/DangerDialog";
import { Column } from "components/table/Column";
import { Table, TableRef } from "components/table/Table";
import { showInfoToastr } from "components/toastr";

import { localizedMoment } from "utils/datetime";
import Network from "utils/network";

import { AccessToken, TokenType, ValidityRequest } from "./types";

type Props = {
  allowToggleValidity: boolean;
  allowDeletion: boolean;
};

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

  public refresh(): void {
    this.tableRef.current?.refresh();
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
          cell={(row: AccessToken) => this.renderDate(row.creationDate)}
        />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="modificationDate"
          header={t("Last Modified")}
          cell={(row: AccessToken) => this.renderDate(row.modificationDate)}
        />
        {(this.props.allowDeletion || this.props.allowToggleValidity) && (
          <Column
            columnClass="text-center"
            headerClass="text-center"
            header={t("Actions")}
            cell={(row: AccessToken) => this.renderActions(row)}
          />
        )}
      </Table>
    );
  }

  private renderExpiration(expirationTime: Date | null): React.ReactNode {
    if (expirationTime === null) {
      return t("No, never expires");
    }

    const isExpired = localizedMoment(expirationTime).isBefore(localizedMoment());
    const param = { expiration: (_str) => this.renderDate(expirationTime) };

    return (
      <span className={isExpired ? "text-danger" : ""}>
        {isExpired ? t("Yes, expired <expiration></expiration>") : t("No, expires <expiration></expiration>", param)}
      </span>
    );
  }

  private renderDate(date: Date): React.ReactNode {
    return <FromNow value={date} />;
  }

  private renderActions(row: AccessToken) {
    return (
      <div className="btn-group">
        {this.props.allowToggleValidity && (
          <>
            <Button
              icon={row.valid ? "fa-ban" : "fa-check"}
              className="btn-default"
              handler={() =>
                // If the token is used, ask for confirmation. Otherwise, directly toggle the validity
                row.hubId !== null || row.peripheralId !== null
                  ? this.setState({ confirmValidityDialog: true, selectedRow: row })
                  : this.onToggleValidity(row)
              }
            >
              {row.valid ? t("Invalidate") : t("Validate")}
            </Button>
            {this.state.selectedRow && (
              <DangerDialog
                id="confirm-validity-modal"
                isOpen={this.state.confirmValidityDialog}
                title={t("Confirm access token modification")}
                content={
                  <div>
                    <div>{this.getValidityConfirmationMessage(this.state.selectedRow)}</div>
                    <div>{t("Confirm if you want to proceed.")}</div>
                  </div>
                }
                onConfirm={() => this.state.selectedRow && this.onToggleValidity(this.state.selectedRow)}
                onClose={() => this.setState({ confirmValidityDialog: false, selectedRow: undefined })}
                submitText={this.state.selectedRow.valid ? t("Invalidate") : t("Validate")}
                submitIcon={this.state.selectedRow.valid ? "fa-ban" : "fa-check"}
              />
            )}
          </>
        )}
        {this.props.allowDeletion && (
          <>
            <Button
              disabled={row.hubId !== null || row.peripheralId !== null}
              title={this.getDeleteTitle(row)}
              className="btn-default"
              icon="fa-trash"
              handler={() => this.setState({ confirmDeleteDialog: true, selectedRow: row })}
            >
              {t("Delete")}
              {this.state.selectedRow && (
                <DangerDialog
                  id="confirm-deletion-modal"
                  isOpen={this.state.confirmDeleteDialog}
                  title={t("Confirm access token deletion")}
                  content={<div>{t("Are you sure you want to delete this token. It is currently not used.")}</div>}
                  onConfirm={() => this.state.selectedRow && this.onDelete(this.state.selectedRow)}
                  onClose={() => this.setState({ confirmDeleteDialog: false, selectedRow: undefined })}
                  submitText={t("Delete")}
                  submitIcon="fa-trash"
                />
              )}
            </Button>
          </>
        )}
      </div>
    );
  }

  private onDelete(row: AccessToken): void {
    Network.del(`/rhn/manager/api/admin/hub/access-tokens/${row.id}`)
      .catch((xhr) => Network.showResponseErrorToastr(xhr))
      .then((response) => {
        this.refresh();
        showInfoToastr("Access token successfully deleted");
      });
  }

  private onToggleValidity(row: AccessToken): void {
    const request: ValidityRequest = { valid: !row.valid };

    Network.post(`/rhn/manager/api/admin/hub/access-tokens/${row.id}/validity`, request)
      .catch((xhr) => Network.showResponseErrorToastr(xhr))
      .then((response) => {
        row.valid = request.valid;
        row.expirationDate = response.data;
        this.tableRef.current?.refresh();
        showInfoToastr("Access token validity successfully chande");
      });
  }

  private getDeleteTitle(row: AccessToken): string {
    const params = { fqdn: row.serverFqdn };
    if (row.hubId !== null) {
      return t("This access token cannot be deleted. It is currently used by hub {fqdn}", params);
    }

    if (row.peripheralId !== null) {
      return t("This access token cannot be deleted. It is currently used by peripheral {fqdn}", params);
    }

    // both the peripheral and the hub are null means the token is not used and we can delete it
    return t("Delete");
  }

  private getValidityConfirmationMessage(row: AccessToken): string {
    const messageGeneratorMap: { [key: string]: (params: object) => string } = {
      "issued-valid-hub": (params) =>
        // token issued for another server currently valid and linked to a hub
        t("Invalidating this access token will prevent the hub {fqdn} from accesssing.", params),
      "issued-valid-peripheral": (params) =>
        // token issued for another server currently valid and linked to a peripheral
        t("Invalidating this access token will prevent the peripheral {fqdn} from accesssing.", params),
      "issued-invalid-hub": (params) =>
        // token issued for another server currently not valid and linked to a hub
        t("Enabling this access token will grant access to the hub {fqdn}.", params),
      "issued-invalid-peripheral": (params) =>
        // token issued for another server currently not valid and linked to a peripheral
        t("Enabling this access token will grant access to the peripheral {fqdn}.", params),
      "consumed-valid-hub": (params) =>
        // token received from another server currently valid and linked to a hub
        t("Invalidating this access token will prevent access to the hub {fqdn}.", params),
      "consumed-valid-peripheral": (params) =>
        // token received from another server currently valid and linked to a peripheral
        t("Invalidating this access token will prevent access to the peripheral {fqdn}.", params),
      "consumed-invalid-hub": (params) =>
        // token received from another server currently not valid and linked to a hub
        t("Enabling this access token will restore access to the hub {fqdn}.", params),
      "consumed-invalid-peripheral": (params) =>
        // token received from another server currently not valid and linked to a peripheral
        t("Enabling this access token will restore access to the peripheral {fqdn}.", params),
    };

    const key = `${this.getTypePart(row.type)}-${this.getValidityPart(row.valid)}-${this.getRolePart(row)}`;
    const messageGenerator = messageGeneratorMap[key];

    if (messageGenerator) {
      return messageGenerator({ fqdn: row.serverFqdn });
    }

    return `invalid row state: ${row.type}, ${row.valid}, ${row.hubId}/${row.peripheralId}`;
  }

  private getTypePart(type: TokenType): "issued" | "consumed" {
    return type === TokenType.ISSUED ? "issued" : "consumed";
  }

  private getValidityPart(valid: boolean): "valid" | "invalid" {
    return valid ? "valid" : "invalid";
  }

  private getRolePart(row): "hub" | "peripheral" | "undefined" {
    if (row.hubId !== null) {
      return "hub";
    }

    if (row.peripheralId !== null) {
      return "peripheral";
    }
    return "undefined";
  }
}
