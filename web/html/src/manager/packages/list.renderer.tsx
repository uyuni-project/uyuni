import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { PackageList } from "./list";

type RendererProps = {
  selected: Array<string>;
  selectedChannel: string | null;
};

export const renderer = (id: string, docsLocale: string, { selected, selectedChannel }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <PackageList docsLocale={docsLocale} selected={selected} selectedChannel={selectedChannel} />
    </>,
    document.getElementById(id)
  );
