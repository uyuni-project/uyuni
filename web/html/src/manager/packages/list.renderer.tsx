/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { PackageList } from "./list";

type RendererProps = {
  selected: string[];
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
