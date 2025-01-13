import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralDetailData } from "../iss_data_props";
import IssPeripheralDetail from "./peripheral-details";

export const renderer = (id: string, peripheralDetail: PeripheralDetailData) => {
  SpaRenderer.renderNavigationReact(<IssPeripheralDetail />, document.getElementById(id));
};
