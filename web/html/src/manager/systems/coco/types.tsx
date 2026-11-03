/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { SystemData } from "components/target-systems";

export type CoCoSystemData = {
  cocoSupport: boolean;
} & SystemData;
