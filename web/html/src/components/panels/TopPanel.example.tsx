/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { TopPanel,  } from "./TopPanel";
import { StorySection } from "manager/storybook/layout";

export default () => {
  return (
    <>
      <h3>TopPanel:</h3>
      <TopPanel title="TopPanel with icon and links" icon="fa-filter" helpUrl="index.html">
        TopPanel content
      </TopPanel>
      <hr></hr>
      <StorySection>
        &lt;TopPanel
        <br /> &nbsp; title="TopPanel with icon and links"
        <br /> &nbsp; icon="fa-filter"
        <br /> &nbsp; helpUrl="index.html"
        <br /> &gt;
        <br /> &nbsp; TopPanel content
        <br /> &lt;/TopPanel&gt;
      </StorySection>
    </>
  );
};
