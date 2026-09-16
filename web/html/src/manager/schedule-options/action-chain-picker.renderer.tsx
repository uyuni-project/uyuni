/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { ActionChainPicker } from "./action-chain-picker";

type Props = {
  actionChains: string;
};

export const renderer = (id: string, props: Props) => {
  let actionChains = [] as any[];
  try {
    actionChains = JSON.parse((props.actionChains || "").replace(/&quot;/g, '"'));
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(<ActionChainPicker actionChains={actionChains} />, document.getElementById(id));
};
