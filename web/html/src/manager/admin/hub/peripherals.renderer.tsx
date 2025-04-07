import SpaRenderer from "core/spa/spa-renderer";

import IssPeripheral from "./peripherals";

export const renderer = (id: string, flashMessage: string) => {
  SpaRenderer.renderNavigationReact(<IssPeripheral flashMessage={flashMessage} />, document.getElementById(id));
};
