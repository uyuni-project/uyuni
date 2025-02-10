import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { AllSystems } from "./all-list";

type RendererProps = {
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export const renderer = (parent: Element, { isAdmin, queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <AllSystems isAdmin={isAdmin} queryColumn={queryColumn} query={query} />
    </>,
    parent
  );
