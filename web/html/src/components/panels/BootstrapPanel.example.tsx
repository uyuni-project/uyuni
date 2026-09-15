/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { BootstrapPanel } from "./BootstrapPanel";

export default () => {
  return (
    <>
      <p>BootstrapPanel with title:</p>
      <BootstrapPanel title="Bootstrap title" footer="testing">
        stuff
      </BootstrapPanel>
    </>
  );
};
