/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import CoCoScansList from "components/CoCoScansList";

export const renderer = (id) => SpaRenderer.renderNavigationReact(<CoCoScansList />, document.getElementById(id));
