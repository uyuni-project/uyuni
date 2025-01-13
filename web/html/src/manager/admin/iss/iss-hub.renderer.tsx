import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralData } from "./iss_data_props";
import IssHub from "./iss-hub";

export const renderer = (id: string, peripherals: Array<PeripheralData>) => {
  SpaRenderer.renderNavigationReact(<IssHub peripherals={peripherals} />, document.getElementById(id));
};
