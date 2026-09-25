/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import * as generatedStories from "./stories.generated";

const storyGroups = Object.groupBy(Object.values(generatedStories), (item) => item.groupName);

export default Object.entries(storyGroups).map(([title, stories]) => ({ title, stories }));
