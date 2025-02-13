import SpaRenderer from "core/spa/spa-renderer";

import AddIssPeripheral from "./add-peripheral";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<AddIssPeripheral />, document.getElementById(id));
};
