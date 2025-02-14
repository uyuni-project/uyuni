import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralListData } from "./iss_data_props";
import IssPeripheral from "./peripherals";

export const renderer = (id: string, peripherals: PeripheralListData[]) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral peripherals={peripherals} />, document.getElementById(id));
};
