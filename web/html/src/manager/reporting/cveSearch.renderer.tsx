import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { CveSearch } from "./cveSearch";

type RendererProps = {
  queryColumn?: string;
  query?: string;
};

export const renderer = (id: string, docsLocale: string, { queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <CveSearch docsLocale={docsLocale} query={query} />
    </>,
    document.getElementById(id)
  );
