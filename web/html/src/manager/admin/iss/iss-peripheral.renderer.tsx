import SpaRenderer from "core/spa/spa-renderer";

import { HubData } from "./iss_data_props";
import IssPeripheral from "./iss-peripheral";

export const renderer = (id: string, hubs: Array<HubData>) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral hubs={hubs} />, document.getElementById(id));
};
