import SpaRenderer from "core/spa/spa-renderer";

import { PeripheralListData } from "components/hub";

import IssPeripheral from "./peripherals";

export const renderer = (id: string, peripherals: PeripheralListData[]) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral peripherals={peripherals} />, document.getElementById(id));
};
