import { type ReactNode, Component } from "react";

import { Button } from "components/buttons";
import { DangerDialog } from "components/dialog/DangerDialog";
import { IssRole } from "components/hub/types";
import { showInfoToastr } from "components/toastr";

import Network from "utils/network";

type Props = {
  /** Specify if the deregistration is for a hub or a peripheral */
  role: IssRole;
  /** The unique identifier of the server */
  id: number;
  /** The fully qualified domain name of the server */
  fqdn: string;
  /** Callback invoked after the deregistration as been performed */
  onDeregistered?: () => void;
};

type State = {
  confirmDeregistration: boolean;
  showDeleteErrorModal: boolean;
};

export class DeregisterServer extends Component<Props, State> {
  public constructor(props: Props) {
    super(props);

    this.state = { confirmDeregistration: false, showDeleteErrorModal: false };
  }

  public render(): ReactNode {
    return (
      <>
        <Button
          className="btn-default btn-danger"
          handler={() => this.setState({ confirmDeregistration: true })}
          text={t("Deregister")}
          title={t("Deregister from the hub")}
          icon="fa-trash"
        />
        <DangerDialog
          id="confirm-deregister-modal"
          title={t("Confirm deregistration")}
          content={<span>{this.getConfirmationMessage()}</span>}
          isOpen={this.state.confirmDeregistration}
          submitText={t("Deregister")}
          submitIcon="fa-trash"
          onConfirm={() => this.onConfirmDeregistration(false)}
          onClose={() => this.setState({ confirmDeregistration: false })}
        />
        <DangerDialog
          id="deregister-error-modal"
          title={t("Error deregistering server")}
          content={this.renderDeregisterErrorModal()}
          isOpen={this.state.showDeleteErrorModal}
          onConfirm={() => this.onConfirmDeregistration(true)}
          onClose={() => this.setState({ showDeleteErrorModal: false })}
          submitText={t("Deregister without cleanup")}
          submitIcon={"fa-trash"}
        />
      </>
    );
  }

  private renderDeregisterErrorModal() {
    return <span>{t("Cleanup timed out. Please check if the machine is reachable.")}</span>;
  }

  private getConfirmationMessage(): string {
    const params = {
      fqdn: this.props.fqdn,
    };

    switch (this.props.role) {
      case IssRole.Hub:
        return t(
          "Are you sure you want to deregister from hub {fqdn}? If you do, you will no longer receive data from it.",
          params
        );

      case IssRole.Peripheral:
        return t(
          "Are you sure you want to deregister peripheral {fqdn}? All the current configuration will be lost.",
          params
        );
      default:
        return "Error: invalid role parameter";
    }
  }

  private onConfirmDeregistration(only_local: boolean): void {
    let resource =
      this.props.role === IssRole.Hub
        ? `/rhn/manager/api/admin/hub/${this.props.id}`
        : `/rhn/manager/api/admin/hub/peripherals/${this.props.id}`;
    if (only_local) {
      resource += `?only_local=true`;
    }
    Network.del(resource)
      .then(
        () => {
          this.onSuccessfullDeregister();
        },
        (xhr) => {
          if (xhr && xhr.status === 503) {
            this.setState({ confirmDeregistration: false, showDeleteErrorModal: true });
          } else {
            Network.showResponseErrorToastr(xhr);
          }
        }
      )
      .finally(() => this.setState({ confirmDeregistration: false }));
  }

  private onSuccessfullDeregister() {
    showInfoToastr(t("The server {fqdn} has been successfully deregistered.", { fqdn: this.props.fqdn }));
    this.props.onDeregistered?.();
  }
}
