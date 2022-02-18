import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { VirtualSystems } from "./virtual-list";

export const renderer = (id: string, docsLocale: string, isAdmin: boolean) =>
  SpaRenderer.renderNavigationReact(
    <VirtualSystems docsLocale={docsLocale} isAdmin={isAdmin} />,
    document.getElementById(id)
  );
