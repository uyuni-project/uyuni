/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import { SsmCounter } from "./ssm-counter";

type RendererProps = {
  count?: number;
};

export const renderer = (id: string, { count }: RendererProps = {}) =>
  SpaRenderer.renderGlobalReact(<SsmCounter count={count} />, document.getElementById(id));
