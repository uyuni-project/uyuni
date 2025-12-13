import { type ReactNode, Component } from "react";

import { AsyncButton, Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { LargeTextAttachment } from "components/large-text-attachment";
import { Messages } from "components/messages/messages";
import { TopPanel } from "components/panels";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";
import Validation from "components/validation";

import Network from "utils/network";

import { ModalEditButton } from "./ModalEditButton";
import {
  MigrationEntry,
  MigrationMessage,
  MigrationMessageLevel,
  MigrationResult,
  MigrationResultCode,
  MigrationVersion,
} from "./types";

type Props = {
  title: string;
  migrationEntries: MigrationEntry[];
  migrateFrom: MigrationVersion;
};

type State = {
  tableModel: MigrationEntry[];
  tableModelValid: boolean;
  loading: boolean;
  submitted: boolean;
  migrationResult: MigrationResult | undefined;
};

export class MigrateSlavesForm extends Component<Props, State> {
  static defaultProps: Partial<Props> = {
    migrationEntries: [],
  };

  constructor(props: Props) {
    super(props);

    const initialModel = [...this.props.migrationEntries];

    this.state = {
      tableModel: initialModel,
      tableModelValid: this.isTableModeValid(initialModel),
      loading: false,
      submitted: false,
      migrationResult: undefined,
    };
  }

  public render(): ReactNode {
    return (
      <TopPanel
        title={this.props.title}
        icon="fa fa-flip-horizontal fa-truck"
        button={
          this.props.migrateFrom === MigrationVersion.v2 && (
            // When migrating from ISSv2 the user needs to add all the information about the remote servers
            <div className="btn-group pull-right">
              <ModalEditButton
                buttonLabel={t("Add Server")}
                buttonIcon="fa-plus"
                modalTitle={t("Set the FQDN of the remote server")}
                fieldLabel={t("Server FQDN")}
                disabled={this.state.loading}
                validators={[Validation.matches(/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*$/)]}
                invalidHint={t("Has to be a valid FQDN address")}
                onSave={(value) => this.onAddServer(value)}
              />
            </div>
          )
        }
      >
        {/* Loading banner for when the submit is in progress */}
        {this.state.loading && this.renderLoadingBanner()}
        {/* Modal dialog to show the problems happened during migration */}
        {this.state.migrationResult && this.renderMigrationResultDialog()}
        {/* When we are migrating from ISSv1 it is possible that we don't have any server to migrate. In this case
         * show an error message instead of the server table. This cannot happen for ISSv2 because the user must always
         * insert the server information manually. */}
        {this.noServerToMigrate() ? this.renderNothingToMigrateMessage() : this.renderServerTable()}
      </TopPanel>
    );
  }

  private onSubmit(): Promise<void> {
    const migratable = this.state.tableModel.filter((entry) => !entry.disabled && entry.selected);

    this.setState({ submitted: false, loading: true });

    return Network.post(`/rhn/manager/api/admin/hub/migrate/${this.props.migrateFrom}`, migratable)
      .then(
        (response) => {
          const migrationResult = response as MigrationResult;
          if (migrationResult.resultCode === MigrationResultCode.SUCCESS) {
            window.pageRenderers?.spaengine?.navigate?.("/rhn/manager/admin/hub/peripherals");
          } else {
            this.setState({ migrationResult });
          }
        },
        (xhr) => Network.showResponseErrorToastr(xhr)
      )
      .finally(() => this.setState({ loading: false, submitted: true }));
  }

  private onAddServer(fqdn: string): void {
    this.setState((prevState) => ({
      tableModel: [
        ...prevState.tableModel,
        {
          id: prevState.tableModel.length,
          fqdn: fqdn,
          accessToken: "",
          rootCA: null,
          disabled: false,
          selected: true,
        } as MigrationEntry,
      ],
    }));
  }

  private renderLoadingBanner(): ReactNode {
    return (
      <Messages
        items={[
          {
            severity: "info",
            text: <p>{t("Peripheral migration in progress: waiting for a response...")}</p>,
          },
        ]}
      />
    );
  }

  private renderMigrationResultDialog(): ReactNode {
    if (this.state.migrationResult === undefined) {
      return <></>;
    }

    return (
      <Dialog
        id="show-migration-result"
        title={
          // Result can either be PARTIAL or ERROR. SUCCESS will bypass this modal
          this.state.migrationResult.resultCode === MigrationResultCode.PARTIAL
            ? t("Migration partially succeeded")
            : t("Migration failed")
        }
        isOpen={true}
        closableModal={false}
        content={
          <>
            <p>
              {this.state.migrationResult.resultCode === MigrationResultCode.PARTIAL
                ? t("Some of the servers where migrated successfully, but some errors happened during the process.")
                : t("The migration process was not successfull and reported the following errors")}
            </p>
            <ul className="mx-5">
              {this.state.migrationResult.messageSet.map((message) => this.renderResultMessage(message))}
            </ul>
          </>
        }
        footer={
          <div className="col-lg-6">
            <div className="btn-group">
              <Button
                id="show-migration-result-close-button"
                className="btn-default"
                handler={() => window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals`)}
                text={t("Close")}
                icon="fa-close"
              />
            </div>
          </div>
        }
      />
    );
  }

  private noServerToMigrate(): boolean {
    if (this.props.migrateFrom === MigrationVersion.v2) {
      return false;
    }

    return this.state.tableModel.length === 0;
  }

  private renderNothingToMigrateMessage(): ReactNode {
    return (
      <>
        <Messages
          items={[
            {
              severity: "info",
              text: <p>{t("There are no ISSv1 slaves to migrate.")}</p>,
            },
          ]}
        />
        <div className="btn-group pull-right">
          <Button
            id="cancel-btn"
            className="btn-default"
            text={t("Back")}
            disabled={this.state.loading}
            handler={() => window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals`)}
          />
        </div>
      </>
    );
  }

  private renderServerTable(): ReactNode {
    return (
      <>
        <Table data={this.state.tableModel} identifier={(row: MigrationEntry) => row.id}>
          <Column
            columnKey="selected"
            header={t("Migrate?")}
            cell={(row: MigrationEntry) => this.renderSelection(row)}
          />
          <Column columnKey="fqdn" header={t("Server FQDN")} cell={(row: MigrationEntry) => row.fqdn} />
          <Column
            columnKey="accessToken"
            header={t("Access Token")}
            cell={(row: MigrationEntry) => this.renderToken(row)}
          />
          <Column
            columnKey="rootCA"
            header={t("Root Certificate Authority")}
            cell={(row: MigrationEntry) => this.renderRootCA(row)}
          />
        </Table>
        <div className="btn-group pull-right">
          <AsyncButton
            id="submit-btn"
            className="btn-primary"
            text={t("Submit")}
            disabled={this.state.loading || !this.state.tableModelValid}
            action={() => this.onSubmit()}
          />
          <Button
            id="cancel-btn"
            className="btn-default"
            text={t("Cancel")}
            disabled={this.state.loading}
            handler={() => window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals`)}
          />
        </div>
      </>
    );
  }

  private renderSelection(row: MigrationEntry): ReactNode {
    return (
      <input
        type="checkbox"
        checked={row.selected}
        disabled={row.disabled || this.state.loading}
        title={
          row.disabled
            ? t("This slave is currently disabled and cannot be selected for migration")
            : t("Check to migrate this server to a peripheral")
        }
        onChange={(event) => {
          const updateModel = this.updateField("selected", row.id, event.target.checked);
          this.setState({ tableModel: updateModel, tableModelValid: this.isTableModeValid(updateModel) });
        }}
      />
    );
  }

  private renderToken(row: MigrationEntry): ReactNode {
    const tokenPresent = row.accessToken !== null && row.accessToken.trim().length > 0;

    return (
      <div className="container pl-0">
        <div className="row align-items-center">
          <div className="col-lg-4">
            <span className={`text-nowrap text-${tokenPresent ? "success" : "danger"}`}>
              <i className={`fa ${tokenPresent ? "fa-check" : "fa-times"}`}></i>
              {tokenPresent ? `${row.accessToken?.substring(0, 10)}...` : t("Not defined")}
            </span>
          </div>
          <div className="col-lg-1">
            <ModalEditButton
              buttonLabel={t("Edit")}
              modalTitle={t("Set access token for {fqdn}", row)}
              fieldLabel={t("Access token")}
              placeholder="eyJhbGci..."
              disabled={row.disabled || this.state.loading || !row.selected}
              value={row.accessToken ?? ""}
              onSave={(value: string) => this.updateToken(row.id, value)}
            />
          </div>
        </div>
      </div>
    );
  }

  private updateToken(id: number, value: string): void {
    const updatedModel = this.updateField("accessToken", id, value);

    // Update the table model and compute its validity
    this.setState({
      tableModel: updatedModel,
      tableModelValid: this.isTableModeValid(updatedModel),
    });
  }

  private renderRootCA(row: MigrationEntry): ReactNode {
    return (
      <div className="container pl-0">
        <div className="row align-items-center">
          <div className="col-lg-3">
            <span className="text-nowrap">
              <i className={`fa ${row.rootCA !== null ? "fa-check" : "fa-ban"}`}></i>
              {row.rootCA !== null ? t("Root CA available") : t("Not needed")}
            </span>
          </div>
          <div className="col-lg-1">
            <LargeTextAttachment
              value={row.rootCA}
              filename={row.fqdn + "_CA.pem"}
              hideMessage={true}
              editable={true}
              downloadable={false}
              presentMessage={t("The root CA is the same of the hub.")}
              absentMessage={t("This server has a different root CA.")}
              editDialogTitle={t("Root Certificate Authority")}
              editMessage={t(
                "Ensure the certificate you are providing for {fqdn} is correct before proceeding. A wrong value will prevent the connection with the hub.",
                row
              )}
              disabled={row.disabled || this.state.loading || !row.selected}
              confirmDeleteMessage={t(
                "Are you sure you want to remove the current Root Certificate Authority for {fqdn}?",
                row
              )}
              onDelete={() => Promise.resolve(this.setState({ tableModel: this.updateField("rootCA", row.id, null) }))}
              onEdit={(value) =>
                Promise.resolve(this.setState({ tableModel: this.updateField("rootCA", row.id, value) }))
              }
            />
          </div>
        </div>
      </div>
    );
  }

  private renderResultMessage(message: MigrationMessage): ReactNode {
    let iconClass: string, iconTitle: string;

    switch (message.severity) {
      case MigrationMessageLevel.INFO:
        iconClass = "fa fa-info-circle";
        iconTitle = t("Info");
        break;

      case MigrationMessageLevel.WARN:
        iconClass = "fa fa-exclamation-triangle text-warning";
        iconTitle = t("Warning");
        break;

      case MigrationMessageLevel.ERROR:
        iconClass = "fa fa-times-circle-o text-danger";
        iconTitle = t("Error");
        break;
    }

    return (
      <li>
        <span>
          <i className={iconClass} title={iconTitle}></i>
          {message.message}
        </span>
      </li>
    );
  }

  private isTableModeValid(entries: MigrationEntry[]): boolean {
    // Filter only the entries that can be migrated and are selected
    const migratable = entries.filter((entry) => !entry.disabled && entry.selected);

    if (migratable.length === 0) {
      return false;
    }

    for (const row of migratable) {
      // Check if FQDN is not empty and has the correct format
      if (row.fqdn.trim().length === 0 || !/^[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*$/.test(row.fqdn)) {
        return false;
      }

      // Check if access token is not empty
      if (row.accessToken === null || row.accessToken.trim().length === 0) {
        return false;
      }
    }

    return true;
  }

  // Updates the specified field of the model for the entry with the given id
  private updateField<K extends keyof MigrationEntry>(
    field: K,
    id: number,
    value: MigrationEntry[K]
  ): MigrationEntry[] {
    return this.state.tableModel.map((entry) => (entry.id === id ? { ...entry, [field]: value } : entry));
  }
}
