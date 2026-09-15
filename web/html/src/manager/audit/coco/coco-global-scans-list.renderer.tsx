/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import CoCoScansList from "components/CoCoScansList";

export const renderer = (id) => SpaRenderer.renderNavigationReact(<CoCoScansList />, document.getElementById(id));
