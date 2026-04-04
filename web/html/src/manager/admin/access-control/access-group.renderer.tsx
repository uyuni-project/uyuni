import SpaRenderer from "core/spa/spa-renderer";

import CreateAccessGroup from "./access-group";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<CreateAccessGroup />, document.getElementById(id));
};
