import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralData } from "./iss_data_props";
import IssHub from "./iss-hubs";

export const renderer = (id: string, peripherals: PeripheralData[]) => {
  SpaRenderer.renderNavigationReact(<IssHub peripherals={peripherals} />, document.getElementById(id));
};
