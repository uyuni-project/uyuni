import SpaRenderer from "core/spa/spa-renderer";

import IssHub from "./hub";
import { PeripheralData } from "./iss_data_props";

export const renderer = (id: string, peripherals: Array<PeripheralData>) => {
  SpaRenderer.renderNavigationReact(<IssHub peripherals={peripherals} />, document.getElementById(id));
};
