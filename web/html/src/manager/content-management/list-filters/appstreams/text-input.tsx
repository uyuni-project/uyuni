/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { Text } from "components/input";

export default function TextInput() {
  return (
    <>
      <Text name="moduleName" label={t("Module Name")} labelClass="col-md-3" divClass="col-md-6" required />
      <Text name="moduleStream" label={t("Stream")} labelClass="col-md-3" divClass="col-md-6" />
    </>
  );
}
