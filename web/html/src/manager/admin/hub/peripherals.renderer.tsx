import SpaRenderer from "core/spa/spa-renderer";

import IssPeripheral from "./peripherals";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral />, document.getElementById(id));
};
