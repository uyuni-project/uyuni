/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { Loading } from "./Loading";

export default () => {
  return (
    <>
      <p>Loading indicator with text:</p>
      <Loading text={t("Loading text")} />
    </>
  );
};
