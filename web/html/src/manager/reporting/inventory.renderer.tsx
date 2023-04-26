import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { SystemsInventory } from "./inventory";

type RendererProps = {
  queryColumn?: string;
  query?: string;
};

export const renderer = (id: string, docsLocale: string, { queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <SystemsInventory docsLocale={docsLocale} queryColumn={queryColumn} query={query} />
    </>,
    document.getElementById(id)
  );
