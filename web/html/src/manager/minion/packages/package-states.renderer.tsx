/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr/toastr";

import PackageStates from "./package-states";

type RendererProps = {
  serverId?: any;
};

export const renderer = (id: string, { serverId }: RendererProps = {}) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <PackageStates serverId={serverId} />
    </>,
    document.getElementById(id)
  );
};
