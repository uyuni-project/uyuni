import * as React from "react";

import { DeregisterServer, HubDetailData, HubDetailsForm, IssRole } from "components/hub";
import { TopPanel } from "components/panels";

export type Props = {
  hub: HubDetailData | null;
};

type State = {
  hub: HubDetailData | null;
};

export class HubDetails extends React.Component<Props, State> {
  public constructor(props: Props) {
    super(props);

    this.state = { hub: props.hub !== null ? { ...props.hub } : null };
  }

  public render(): React.ReactNode {
    return (
      <TopPanel
        title={t("Hub Details")}
        icon="fa-cogs"
        helpUrl="reference/admin/hub/hub-details.html"
        button={
          <div className="btn-group pull-right">
            {this.state.hub !== null && (
              <DeregisterServer
                role={IssRole.Hub}
                id={this.state.hub.id}
                fqdn={this.state.hub.fqdn}
                onDeregistered={() => this.setState({ hub: null })}
              />
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
            <HubDetailsForm model={this.state.hub} editable={true} />
          </>
        )}
      </TopPanel>
    );
  }
}
