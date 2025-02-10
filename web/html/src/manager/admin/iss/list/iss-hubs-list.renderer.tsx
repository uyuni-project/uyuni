import SpaRenderer from "core/spa/spa-renderer";

import { HubData } from "../iss_data_props";
import IssHubsList from "./iss-hubs-list";

export const renderer = (id: string, hubsList: HubData[]) => {
  SpaRenderer.renderNavigationReact(<IssHubsList hubs={hubsList} />, document.getElementById(id));
};
