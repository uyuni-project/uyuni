import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { VirtualSystems } from "./virtual-list";

type RendererProps = {
  pageSize: number;
  isAdmin: boolean;
};

export const renderer = (id: string, docsLocale: string, { pageSize, isAdmin }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <VirtualSystems docsLocale={docsLocale} pageSize={pageSize} isAdmin={isAdmin} />
    </>,
    document.getElementById(id)
  );
