import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { NotificationMessages } from "./notification-messages";

export const renderer = (parent: Element) => SpaRenderer.renderNavigationReact(<NotificationMessages />, parent);
