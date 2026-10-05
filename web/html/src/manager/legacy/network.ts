/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import Network from "utils/network";

declare global {
  interface Window {
    /** The standard network layer made globally available for legacy integrations such as DWR etc */
    network?: typeof Network;
  }
}

window.network = Network;
