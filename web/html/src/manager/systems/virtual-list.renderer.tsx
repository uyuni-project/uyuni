import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { VirtualSystems } from "./virtual-list";

type RendererProps = {
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export const renderer = (parent: Element, { isAdmin, queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <VirtualSystems isAdmin={isAdmin} queryColumn={queryColumn} query={query} />
    </>,
    parent
  );
