import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { AllSystems } from "./all-list";

type RendererProps = {
  pageSize: number;
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export const renderer = (id: string, docsLocale: string, { pageSize, isAdmin, queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <AllSystems
        docsLocale={docsLocale}
        pageSize={pageSize}
        isAdmin={isAdmin}
        queryColumn={queryColumn}
        query={query}
      />
    </>,
    document.getElementById(id)
  );
