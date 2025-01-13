import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { PeripheralListData, PeripheralsList } from "components/hub";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { HelpLink } from "components/utils";

type Props = {
  peripherals: PeripheralListData[];
};

const IssPeripheral = (peripheralsList: Props) => {
  const [peripherals] = useState(peripheralsList.peripherals);

  const title = (
    <div className="spacewalk-toolbar-h1">
      <h1>
        <i className="fa fa-cogs"></i>
        &nbsp;
        {t("ISS - Hub Configuration")}
        &nbsp;
        <HelpLink url="reference/admin/iss-peripheral.html" />
      </h1>
    </div>
  );

  const addPeripheral = () => {
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/hub/peripherals/register`);
  };

  let pageContent = <PeripheralsList peripherals={peripherals} />;

  return (
    <div className="responsive-wizard">
      {title}
      <SectionToolbar>
        <div className="action-button-wrapper">
          <div className="btn-group">
            <Button
              id="addPeripheral"
              icon="fa-plus"
              className={"btn-default"}
              text={t("Add Peripheral")}
              handler={addPeripheral}
            />
          </div>
        </div>
      </SectionToolbar>
      <span>
        <h1>Known Peripherals instances</h1>
      </span>
      {pageContent}
    </div>
  );
};

export default hot(withPageWrapper(IssPeripheral));
