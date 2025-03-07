import SpaRenderer from "core/spa/spa-renderer";

import IssPeripheralsList from "./peripherals-list";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<IssPeripheralsList />, document.getElementById(id));
};
