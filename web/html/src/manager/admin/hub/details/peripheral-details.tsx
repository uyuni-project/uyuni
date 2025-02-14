import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { SectionToolbar } from "components/section-toolbar/section-toolbar";
import { HelpLink } from "components/utils";

import { PeripheralDetailData } from "../iss_data_props";

export type PeripheralDetailProp = {
  peripheral: PeripheralDetailData;
};

const IssPeripheralDetail = (peripheralDetail: PeripheralDetailProp) => {
  const [peripheral] = useState(peripheralDetail.peripheral);

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
    window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/admin/iss/add/peripheral`);
  };

  let pageContent = <IssPeripheralsList peripherals={peripherals} />;

  return (
    <div className="responsive-wizard">
      {title}
      <SectionToolbar>
        <div className="action-button-wrapper">
          <div className="btn-group">
            <Button
              id="addPeripheral"
              icon="fa-plus"
              className={"btn-success"}
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

export default hot(withPageWrapper(IssPeripheralDetail));
