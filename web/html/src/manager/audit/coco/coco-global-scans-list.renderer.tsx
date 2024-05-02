import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import CoCoScansList from "components/CoCoScansList";

export const renderer = (id) => SpaRenderer.renderNavigationReact(<CoCoScansList />, document.getElementById(id));
