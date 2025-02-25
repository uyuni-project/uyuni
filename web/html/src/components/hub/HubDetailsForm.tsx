import * as React from "react";

import { FromNow, HumanDateTime } from "components/datetime";
import { HubDetailData } from "components/hub/types";
import { LargeTextAttachment } from "components/large-text-attachment";
import { showInfoToastr } from "components/toastr";

import { localizedMoment } from "utils/datetime";
import Network from "utils/network";

type Props = {
  model: HubDetailData;
  editable: boolean;
};

type State = {
  model: HubDetailData;
};

export class HubDetailsForm extends React.Component<Props, State> {
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
                  filename="root-ca.pem"
                  presentMessage={t("A customized Root Certificate Authority is currently configured for this hub.")}
                  absentMessage={t("This hub is currently using the default Root Certificate Authority.")}
                  editDialogTitle={t("Root Certificate Authority")}
                  editMessage={t(
                    "Ensure the certificate you are providing is correct before proceeding. A wrong value will break the connection with the hub."
                  )}
                  confirmDeleteMessage={t(
                    "Are you sure you want to delete the current Root Certificate Authority? This change may break the connection with the hub."
                  )}
                  editable={this.props.editable}
                  onDelete={() => {
                    return Network.del(`/rhn/manager/api/admin/hub/${this.state.model.id}/root-ca`).then(
                      (data) => {
                        showInfoToastr(t("Root Certificate Authority successfully modified."));
                        // Update the root ca in the model
                        this.setState((prevState) => ({
                          ...prevState,
                          model: { ...prevState.model, rootCA: null },
                        }));
                      },
                      (err) => Network.showResponseErrorToastr(err)
                    );
                  }}
                  onEdit={(value) => {
                    return Network.post(`/rhn/manager/api/admin/hub/${this.state.model.id}/root-ca`, {
                      rootCA: value,
                    }).then(
                      (data) => {
                        showInfoToastr(t("Root Certificate Authority successfully updated."));
                        // Update the root ca in the model
                        this.setState((prevState) => ({
                          ...prevState,
                          model: { ...prevState.model, rootCA: value },
                        }));
                      },
                      (err) => Network.showResponseErrorToastr(err)
                    );
                  }}
                />
              </div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("GPG Public Key:")}</strong>
              </div>
              <div className="col-md-10">
                <LargeTextAttachment
                  value={this.state.model.gpgKey}
                  filename="gpg-pub.key"
                  presentMessage={t("A customized GPG key is currently configured for this hub.")}
                  absentMessage={t("This hub is currently not using a custom GPG key.")}
                  editable={false}
                />
              </div>
            </div>
          </li>
          <li className="list-group-item">
            <div className="row">
              <div className="col-md-2">
                <strong>{t("Mirror credentials:")}</strong>
              </div>
              <div className="col-md-10">{this.state.model.sccUsername}</div>
            </div>
          </li>
        </ul>
      </div>
    );
  }
}
