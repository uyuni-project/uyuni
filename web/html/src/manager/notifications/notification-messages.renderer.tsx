import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { NotificationMessages } from "./notification-messages";

export const renderer = (id) =>
  SpaRenderer.renderNavigationReact(<NotificationMessages />, document.getElementById(id));
