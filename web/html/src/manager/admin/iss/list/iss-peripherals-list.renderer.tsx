import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralData } from "../iss_data_props";
import IssPeripheralsList from "./iss-peripherals-list";

export const renderer = (id: string, peripheralsList: PeripheralData[]) => {
  SpaRenderer.renderNavigationReact(<IssPeripheralsList peripherals={peripheralsList} />, document.getElementById(id));
};
