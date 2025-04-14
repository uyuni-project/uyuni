import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { NotificationList } from "./notifications-list";
import { NotificationType } from "./types";

export const renderer = (id: string, notificationTypes: Array<NotificationType>) =>
  SpaRenderer.renderNavigationReact(
    <NotificationList notificationTypes={notificationTypes} />,
    document.getElementById(id)
  );
