import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import MonitoringAdmin from "./monitoring-admin";

export const renderer = (parent: Element, props: { isUyuni: boolean }) => {
  SpaRenderer.renderNavigationReact(<MonitoringAdmin isUyuni={props.isUyuni} />, parent);
};
