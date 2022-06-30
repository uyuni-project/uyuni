import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AllSystems } from "./all-list";

export const renderer = (id: string, docsLocale: string, isAdmin: boolean) =>
  SpaRenderer.renderNavigationReact(
    <AllSystems docsLocale={docsLocale} isAdmin={isAdmin} />,
    document.getElementById(id)
  );
