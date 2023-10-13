import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { VirtualSystems } from "./virtual-list";

type RendererProps = {
  isAdmin: boolean;
};

export const renderer = (id: string, docsLocale: string, { isAdmin }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <VirtualSystems docsLocale={docsLocale} isAdmin={isAdmin} />
    </>,
    document.getElementById(id)
  );
