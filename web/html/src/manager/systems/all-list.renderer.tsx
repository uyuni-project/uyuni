import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AllSystems } from "./all-list";

type RendererProps = {
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export const renderer = (id: string, docsLocale: string, { isAdmin, queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <AllSystems docsLocale={docsLocale} isAdmin={isAdmin} queryColumn={queryColumn} query={query} />,
    document.getElementById(id)
  );
