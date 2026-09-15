/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import SpaRenderer from "core/spa/spa-renderer";

import { HeaderSearch } from "./search";

SpaRenderer.renderGlobalReact(<HeaderSearch />, document.getElementById("header-search"));
