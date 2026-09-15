/*
  SPDX-License-Identifier: GPL-2.0-Only
  SPDX-FileCopyrightText: 2026 SUSE LLC
*/
import * as generatedStories from "./stories.generated";

const storyGroups = Object.groupBy(Object.values(generatedStories), (item) => item.groupName);

export default Object.entries(storyGroups).map(([title, stories]) => ({ title, stories }));
