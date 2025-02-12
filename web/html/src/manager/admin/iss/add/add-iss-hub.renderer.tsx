import SpaRenderer from "core/spa/spa-renderer";

import AddIssHub from "./add-iss-hub";

export const renderer = (id: string) => {
  SpaRenderer.renderNavigationReact(<AddIssHub />, document.getElementById(id));
};
