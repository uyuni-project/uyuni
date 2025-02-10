import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { PackageList } from "./list";

type RendererProps = {
  selected: Array<string>;
  selectedChannel: string | null;
};

export const renderer = (parent: Element, { selected, selectedChannel }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <PackageList selected={selected} selectedChannel={selectedChannel} />
    </>,
    parent
  );
