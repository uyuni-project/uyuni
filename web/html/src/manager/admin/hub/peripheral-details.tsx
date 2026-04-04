import { type ReactNode, Component } from "react";

import { Button } from "components/buttons";
import { DeregisterServer, IssRole, PeripheralDetailData, ServerDetailsForm } from "components/hub";
import { TopPanel } from "components/panels";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";

export type Props = {
  peripheral: PeripheralDetailData;
};

export class PeripheralDetails extends Component<Props> {
  public constructor(props: Props) {
    super(props);
  }

  public render(): ReactNode {
    return (
      <TopPanel
        title={t("{fqdn} - Peripheral Details", this.props.peripheral)}
        icon="fa-cogs"
        helpUrl="specialized-guides/large-deployments/hub-online-sync.html#_access_peripheral_server_details_from_the_hub_server"
      >
        <SectionToolbar>
          <div className="action-button-wrapper">
            <div className="btn-group pull-right">
              <DeregisterServer
                role={IssRole.Peripheral}
                id={this.props.peripheral.id}
                fqdn={this.props.peripheral.fqdn}
                onDeregistered={() =>
                  // Go back to the peripheral list
                  window.pageRenderers?.spaengine?.navigate?.("/rhn/manager/admin/hub/peripherals")
                }
              />
            </div>
          </div>
          <div className="selector-button-wrapper">
            <div className="btn-group pull-left">
              <Button
                className="btn-default"
                icon="fa-chevron-left"
                text={t("Back to list")}
                handler={() => window.pageRenderers?.spaengine?.navigate?.("/rhn/manager/admin/hub/peripherals")}
              />
            </div>
          </div>
        </SectionToolbar>
        <p>
          {t(
            "The server {fqdn} is currently registered as peripheral of this hub. " +
              "This page shows the details of the peripheral server.",
            this.props.peripheral
          )}
        </p>
        <ServerDetailsForm model={this.props.peripheral} editable={true} />
      </TopPanel>
    );
  }
}
