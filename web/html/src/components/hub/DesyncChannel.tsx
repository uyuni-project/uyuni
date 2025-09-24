import * as React from "react";

import { Button } from "components/buttons";
import { DangerDialog } from "components/dialog/DangerDialog";
import { showInfoToastr } from "components/toastr";

import Network from "utils/network";

type Props = {
  /** Name of the channel */
  name: string;
  /** Label of the channel */
  label: string;
  /** the peripheral id */
  peripheralId: number;
  /** Callback invoked after the desync as been performed */
  onDesync?: () => void;
};

type State = {
  confirmDesync: boolean;
};

export class DesyncChannel extends React.Component<Props, State> {
  public constructor(props: Props) {
    super(props);
    this.state = { confirmDesync: false };
  }

  public render(): React.ReactNode {
    return (
      <>
        <Button
          className="btn-default btn-danger"
          handler={() => this.setState({ confirmDesync: true })}
          text={t("Desync")}
          title={t("Desync Channel")}
          icon="fa-minus"
        />
        <DangerDialog
          id="confirm-deregister-modal"
          title={t("Confirm Desync")}
          content={<span>{this.getConfirmationMessage()}</span>}
          isOpen={this.state.confirmDesync}
          submitText={t("Desync")}
          submitIcon="fa-minus"
          onConfirm={() => this.onConfirmDesync()}
          onClose={() => this.setState({ confirmDesync: false })}
        />
      </>
    );
  }

  private getConfirmationMessage(): string {
    return t("Desync channels?");
  }

  private onConfirmDesync(): void {
    const resource = `/rhn/manager/api/admin/hub/peripherals/${this.props.peripheralId}/sync-channels`;

    Network.del(resource, [this.props.label])
      .then(
        () => {
          showInfoToastr(t("The channels have been successfully desynced."));
          this.props.onDesync?.();
        },
        (xhr) => Network.showResponseErrorToastr(xhr)
      )
      .finally(() => this.setState({ confirmDesync: false }));
  }
}
