/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import _unescape from "lodash/unescape";

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { VirtualSystems } from "./virtual-list";

type RendererProps = {
  isAdmin: boolean;
  queryColumn?: string;
  query?: string;
};

export const renderer = (id: string, docsLocale: string, { isAdmin, queryColumn, query }: RendererProps) =>
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <VirtualSystems docsLocale={docsLocale} isAdmin={isAdmin} queryColumn={queryColumn} query={_unescape(query)} />
    </>,
    document.getElementById(id)
  );
