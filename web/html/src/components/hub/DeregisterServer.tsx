import * as React from "react";

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
};

export class DeregisterServer extends React.Component<Props, State> {
  public constructor(props: Props) {
    super(props);

    this.state = { confirmDeregistration: false };
  }

  public render(): React.ReactNode {
    return (
      <>
        <Button
          className="btn-default btn-danger"
          handler={() => this.setState({ confirmDeregistration: true })}
          text={t("Deregister")}
          title={t("Deregister from the hub")}
          icon="fa-minus"
        />
        <DangerDialog
          id="confirm-deregister-modal"
          title={t("Confirm deregistration")}
          content={<span>{this.getConfirmationMessage()}</span>}
          isOpen={this.state.confirmDeregistration}
          submitText={t("Deregister")}
          submitIcon="fa-minus"
          onConfirm={() => this.onConfirmDeregistration()}
          onClose={() => this.setState({ confirmDeregistration: false })}
        />
      </>
    );
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
          "Are you sure you want to derigester peripheral {fqdn}? All the current configuration will be lost.",
          params
        );
      default:
        return "Error: invalid role parameter";
    }
  }

  private onConfirmDeregistration(): void {
    const resource =
      this.props.role === IssRole.Hub
        ? `/rhn/manager/api/admin/hub/${this.props.id}`
        : `/rhn/manager/api/admin/hub/peripherals/${this.props.id}`;
    Network.del(resource)
      .then(
        (_response) => {
          showInfoToastr(t("The server {fqdn} has been successfully deregistered.", { fqdn: this.props.fqdn }));

          // Invoke the callback if present
          this.props.onDeregistered?.();
        },
        (xhr) => Network.showResponseErrorToastr(xhr)
      )
      .finally(() => this.setState({ confirmDeregistration: false }));
  }
}
