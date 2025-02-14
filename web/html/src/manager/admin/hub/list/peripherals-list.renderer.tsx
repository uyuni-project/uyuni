import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralListData } from "../iss_data_props";
import IssPeripheralsList from "./peripherals-list";

export const renderer = (id: string, peripheralsList: PeripheralListData[]) => {
  SpaRenderer.renderNavigationReact(<IssPeripheralsList peripherals={peripheralsList} />, document.getElementById(id));
};
