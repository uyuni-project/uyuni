import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralListData } from "./iss-list-data-props";
import IssPeripheralsList from "./iss-peripherals-list";

export const renderer = (id: string, peripheralsList: PeripheralListData) => {
  SpaRenderer.renderNavigationReact(
    <IssPeripheralsList peripherals={peripheralsList.peripherals} />,
    document.getElementById(id)
  );
};
