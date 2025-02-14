import SpaRenderer from "core/spa/spa-renderer";

import HubDetails from "./hub-details";
import { HubDetailData } from "./iss_data_props";

export const renderer = (id: string, hubDetailsData: HubDetailData) => {
  SpaRenderer.renderNavigationReact(<HubDetails hub={hubDetailsData} />, document.getElementById(id));
};
