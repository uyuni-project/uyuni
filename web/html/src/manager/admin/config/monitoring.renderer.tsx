import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import MonitoringAdmin from "./monitoring-admin";

export const renderer = (id: string, isUyuni: boolean) => {
  SpaRenderer.renderNavigationReact(<MonitoringAdmin isUyuni={isUyuni} />, document.getElementById(id));
};
