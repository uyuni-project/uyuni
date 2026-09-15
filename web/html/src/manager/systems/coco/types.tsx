/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { SystemData } from "components/target-systems";

export type CoCoSystemData = {
  cocoSupport: boolean;
} & SystemData;
