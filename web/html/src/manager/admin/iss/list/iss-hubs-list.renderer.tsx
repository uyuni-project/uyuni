import SpaRenderer from "core/spa/spa-renderer";

import IssHubsList from "./iss-hubs-list";
import { HubListData } from "./iss-list-data-props";

export const renderer = (id: string, hubsList: HubListData) => {
  SpaRenderer.renderNavigationReact(<IssHubsList hubs={hubsList.hubs} />, document.getElementById(id));
};
