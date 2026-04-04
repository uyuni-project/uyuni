import { type ReactNode, Component } from "react";

import { Button } from "components/buttons";
import { DangerDialog } from "components/dialog/DangerDialog";
import { DeregisterServer, HubDetailData, IssRole, ServerDetailsForm } from "components/hub";
import { TopPanel } from "components/panels";
import { showInfoToastr } from "components/toastr";

import Network from "utils/network";

export type Props = {
  hub: HubDetailData | null;
};

type State = {
  hub: HubDetailData | null;
  confirmSyncBunch: boolean;
};

export class HubDetails extends Component<Props, State> {
  public constructor(props: Props) {
    super(props);

    this.state = {
      hub: props.hub !== null ? { ...props.hub } : null,
      confirmSyncBunch: false,
    };
  }

  private renderSyncBunch(): ReactNode {
    return (
      <>
        <Button
          className="btn-default btn-secondary"
          handler={() => this.setState({ confirmSyncBunch: true })}
          text={t("Sync Channels")}
          title={t("Schedule Sync Channels task")}
          icon="fa-refresh"
        />
        <DangerDialog
          id="confirm-sync-bunch-modal"
          title={t("Confirm channels synchronization")}
          content={<span>{t("Do you want to schedule a channels synchronization from the hub?")}</span>}
          isOpen={this.state.confirmSyncBunch}
          submitText={t("Schedule")}
          submitIcon="fa-refresh"
          onConfirm={() => this.onConfirmSyncBunch()}
          onClose={() => this.setState({ confirmSyncBunch: false })}
        />
      </>
    );
  }

  public render(): ReactNode {
    return (
      <TopPanel
        title={t("Hub Details")}
        icon="fa-cogs"
        helpUrl="specialized-guides/large-deployments/hub-online-sync.html#_access_hub_server_details_from_the_peripheral_server"
        button={
          <div className="btn-group pull-right">
            {this.state.hub !== null && (
              <>
                <DeregisterServer
                  role={IssRole.Hub}
                  id={this.state.hub.id}
                  fqdn={this.state.hub.fqdn}
                  onDeregistered={() => this.setState({ hub: null })}
                />
                {this.renderSyncBunch()}
              </>
            )}
          </div>
        }
      >
        {this.state.hub === null ? (
          <div>
            <p>
              {t(
                "No hub is currently configured for this server. " +
                  "Refresh this page after configuring this server as a peripheral, to see the hub details"
              )}
            </p>
          </div>
        ) : (
          <>
            <p>
              {t(
                "This server is currently registered as peripheral of hub {fqdn}. " +
                  "This page shows the details of the hub server.",
                {
                  fqdn: this.state.hub.fqdn,
                }
              )}
            </p>
            <ServerDetailsForm model={this.state.hub} editable={true} />
          </>
        )}
      </TopPanel>
    );
  }

  private onConfirmSyncBunch(): void {
    const resource = `/rhn/manager/api/admin/hub/sync-bunch`;
    Network.post(resource)
      .then(
        () => {
          showInfoToastr(t("Successfully scheduled a channels synchronization."));
        },
        (xhr) => Network.showResponseErrorToastr(xhr)
      )
      .finally(() => this.setState({ confirmSyncBunch: false }));
  }
}
