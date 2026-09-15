/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import { TopPanel } from "./TopPanel";

export default () => {
  return (
    <>
      <p>TopPanel with icon and help link:</p>
      <TopPanel title="TopPanel with icon and links" icon="fa-filter" helpUrl="index.html">
        stuff
      </TopPanel>
    </>
  );
};
