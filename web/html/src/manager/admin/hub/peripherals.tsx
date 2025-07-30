import { hot } from "react-hot-loader/root";

import * as React from "react";

import { Button, DropdownButton, LinkButton } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { PeripheralsList } from "components/hub";
import { TopPanel } from "components/panels";
import { MessagesContainer, showSuccessToastr } from "components/toastr";

type Props = {
  flashMessage?: string;
};

const IssPeripheral = (props: Props) => {
  React.useEffect(() => {
    if (props.flashMessage !== undefined && props.flashMessage.length > 0) {
      showSuccessToastr(props.flashMessage);
    }
  }, []);

  return (
    <>
      <MessagesContainer />
      <TopPanel
        title={t("Peripherals Configuration")}
        icon="spacewalk-icon-suse-manager"
        helpUrl="reference/admin/hub/peripherals-configuration.html"
        button={
          <div className="btn-group pull-right">
            <Button
              id="addPeripheral"
              icon="fa-plus"
              className="btn-primary"
              text={t("Add Peripheral")}
              handler={() => window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals/register`)}
            />
            <DropdownButton
              text={t("Migrate")}
              icon="fa-plus"
              title={t("Migrate an existing configuration of a previous version of ISS")}
              className="btn-primary"
              items={[
                <LinkButton
                  id="issue-btn-link"
                  key="issue"
                  className="btn-link"
                  text={t("Migrate ISS v1...")}
                  handler={() =>
                    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals/migrate-from-v1`)
                  }
                />,
                <LinkButton
                  id="store-btn-link"
                  key="store"
                  className="btn-link"
                  text={t("Migrate ISS v2...")}
                  handler={() =>
                    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals/migrate-from-v2`)
                  }
                />,
              ]}
            />
          </div>
        }
      >
        <PeripheralsList />
      </TopPanel>
    </>
  );
};

export default hot(withPageWrapper(IssPeripheral));
