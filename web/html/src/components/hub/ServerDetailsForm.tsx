import * as React from "react";

import { AsyncButton, LinkButton } from "components/buttons";
import { FromNow, HumanDateTime } from "components/datetime";
import { HubDetailData, IssRole, IssServerDetailData, PeripheralDetailData } from "components/hub/types";
import { LargeTextAttachment } from "components/large-text-attachment";
import { showInfoToastr } from "components/toastr";

import { localizedMoment } from "utils/datetime";
import Network from "utils/network";

// Localized messages that depend on the role of the server
const roleBasedMessages = {
  rootCA: {
    presentMessage: {
      HUB: t("A customized Root Certificate Authority is currently configured for this hub."),
      PERIPHERAL: t("A customized Root Certificate Authority is currently configured for this peripheral."),
    },
    absentMessage: {
      HUB: t("This hub is currently using the default Root Certificate Authority."),
      PERIPHERAL: t("This peripheral is currently using the default Root Certificate Authority."),
    },
    editMessage: {
      HUB: t(
        "Ensure the certificate you are providing is correct before proceeding. A wrong value will break the connection with the hub."
      ),
      PERIPHERAL: t(
        "Ensure the certificate you are providing is correct before proceeding. A wrong value will break the connection with the peripheral."
      ),
    },
    confirmDeleteMessage: {
      HUB: t(
        "Are you sure you want to delete the current Root Certificate Authority? This change may break the connection with the hub."
      ),
      PERIPHERAL: t(
        "Are you sure you want to delete the current Root Certificate Authority? This change may break the connection with the peripheral."
      ),
    },
  },
};

type Props = {
  model: IssServerDetailData;
  editable: boolean;
};

type State = {
  model: IssServerDetailData;
};

export class ServerDetailsForm extends React.Component<Props, State> {
  public constructor(props: Props) {
    super(props);

    this.state = {
      model: { ...this.props.model },
    };
  }

  public render(): React.ReactNode {
    return (
      <div className="panel panel-default" key="overview">
        <ul className="list-group">
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Server FQDN:")}</strong>
              </div>
              <div className="col-md-10">{this.state.model.fqdn}</div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Registration date:")}</strong>
              </div>
              <div className="col-md-10">
                <HumanDateTime value={localizedMoment(this.state.model.created).tz(localizedMoment.userTimeZone)} />
              </div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Last modified:")}</strong>
              </div>
              <div className="col-md-10">
                <FromNow value={localizedMoment(this.state.model.modified).tz(localizedMoment.userTimeZone)} />
              </div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Root Certificate Authority:")}</strong>
              </div>
              <div className="col-md-10">
                <LargeTextAttachment
                  value={this.state.model.rootCA}
                  filename={`${this.state.model.fqdn}_CA`}
                  presentMessage={roleBasedMessages.rootCA.presentMessage[this.state.model.role]}
                  absentMessage={roleBasedMessages.rootCA.absentMessage[this.state.model.role]}
                  editDialogTitle={t("Root Certificate Authority")}
                  editMessage={roleBasedMessages.rootCA.editMessage[this.state.model.role]}
                  confirmDeleteMessage={roleBasedMessages.rootCA.confirmDeleteMessage[this.state.model.role]}
                  editable={this.props.editable}
                  onDelete={() => this.onDeleteRootCA()}
                  onEdit={(value) => this.onEditRootCA(value)}
                />
              </div>
            </div>
          </li>
          {this.state.model.role === IssRole.Hub && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("GPG Public Key:")}</strong>
                </div>
                <div className="col-md-10">
                  <LargeTextAttachment
                    value={(this.state.model as HubDetailData).gpgKey}
                    filename="gpg-pub.key"
                    presentMessage={t("A customized GPG key is currently configured for this hub.")}
                    absentMessage={t("This hub is currently not using a custom GPG key.")}
                    editable={false}
                  />
                </div>
              </div>
            </li>
          )}
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Mirror credentials:")}</strong>
              </div>
              <div className="col-md-10">
                <p>{this.state.model.sccUsername}</p>
                {this.state.model.role === IssRole.Peripheral && (
                  <div className="btn-group pull-right">
                    <AsyncButton
                      className="btn-default"
                      text={t("Regenerate credentials")}
                      icon="fa-refresh"
                      action={() => this.onRegenerateCredentials()}
                    />
                  </div>
                )}
              </div>
            </div>
          </li>
          {this.state.model.role === IssRole.Peripheral && (
            <li className="list-group-item">
              <div className="row">
                <div className="col-md-2">
                  <strong>{t("Synchronized channels:")}</strong>
                </div>
                <div className="col-md-10">
                  <p>{this.getChannelSyncSummary()}</p>
                  {this.props.editable && (
                    <div className="btn-group pull-right">
                      <LinkButton
                        className="btn-default"
                        text={t("Edit channels")}
                        icon="fa-pencil"
                        href={`/rhn/manager/admin/hub/peripherals/${this.state.model.id}/sync-channels`}
                      />
                    </div>
                  )}
                </div>
              </div>
            </li>
          )}
        </ul>
      </div>
    );
  }
  private getChannelSyncSummary(): string {
    if (this.state.model.role !== IssRole.Peripheral) {
      return "";
    }

    const peripheralData = this.state.model as PeripheralDetailData;

    if (peripheralData.nSyncedChannels === 1) {
      return t("One syncronized channel");
    }

    if (peripheralData.nSyncedOrgs === 1) {
      return t("{nSyncedChannels} syncronized channels, from one organization", peripheralData);
    }

    return t("{nSyncedChannels} syncronized channels, from {nSyncedOrgs} different organizations", peripheralData);
  }

  private onDeleteRootCA(): Promise<void> {
    return Network.del(this.getEditFieldApiUrl("root-ca", this.state.model)).then(
      (_data) => {
        showInfoToastr(t("Root Certificate Authority successfully modified."));
        // Update the root ca in the model
        this.setState((prevState) => ({
          ...prevState,
          model: { ...prevState.model, rootCA: null },
        }));
      },
      (err) => Network.showResponseErrorToastr(err)
    );
  }

  private onEditRootCA(value: string): Promise<void> {
    return Network.post(this.getEditFieldApiUrl("root-ca", this.state.model), { rootCA: value }).then(
      (_data) => {
        showInfoToastr(t("Root Certificate Authority successfully updated."));
        // Update the root ca in the model
        this.setState((prevState) => ({
          ...prevState,
          model: { ...prevState.model, rootCA: value },
        }));
      },
      (err) => Network.showResponseErrorToastr(err)
    );
  }

  private onRegenerateCredentials(): Promise<void> {
    const apiUrl = this.getEditFieldApiUrl("credentials", this.state.model);
    return Network.post(apiUrl).then(
      (response) => {
        showInfoToastr(t("Mirror credentials successfully regenerated."));
        this.setState((prevState) => ({ model: { ...prevState.model, sccUsername: response.data } }));
      },
      (err) => Network.showResponseErrorToastr(err)
    );
  }

  private getEditFieldApiUrl(field: string, model: IssServerDetailData): string {
    const resource = model.role === IssRole.Hub ? "hub" : "hub/peripherals";
    const serverId = model.id;

    return `/rhn/manager/api/admin/${resource}/${serverId}/${field}`;
  }
}
