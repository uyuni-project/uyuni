/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { OvalState, SystemCve } from "./system-cve";

// See java/core/src/main/resources/com/suse/manager/webui/templates/minion/system-cve.jade
export interface SystemCveRendererProps {
  serverId: number;
  ovalState: OvalState;
}

export const renderer = (id: string, props?: Partial<SystemCveRendererProps>) => {
  if (props?.serverId === undefined || props?.ovalState === undefined) {
    return;
  }

  SpaRenderer.renderNavigationReact(
    <SystemCve serverId={props.serverId} ovalState={props.ovalState} />,
    document.getElementById(id)
  );
};
