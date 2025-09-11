import { hot } from "react-hot-loader/root";

import * as React from "react";

import { Button } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { PeripheralsList } from "components/hub";
import { TopPanel } from "components/panels";
import { MessagesContainer } from "components/toastr";

const IssPeripheral = () => {
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
              className="btn-default"
              text={t("Add Peripheral")}
              handler={() => window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals/register`)}
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
