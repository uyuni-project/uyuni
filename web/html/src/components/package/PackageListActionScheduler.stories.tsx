/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Column } from "components/table/Column";

import { PackageListActionScheduler } from "./PackageListActionScheduler";
import { type PackageFixture, installPackageMocks } from "./story-fixtures";

const packageColumns = [
  <Column key="summary" columnKey="summary" header="Summary" cell={(item: PackageFixture) => item.summary} />,
  <Column key="arch" columnKey="arch" header="Architecture" cell={(item: PackageFixture) => item.arch} />,
];

const meta = {
  title: "Compositions/Package Management/PackageListActionScheduler",
  component: PackageListActionScheduler,
  decorators: [
    (Story) => (
      <div
        onClickCapture={(event) => {
          const link = event.target instanceof Element ? event.target.closest("a[href^='/rhn/']") : null;
          if (link) {
            event.preventDefault();
            action("package link clicked")(link.getAttribute("href"));
          }
        }}
      >
        <Story />
      </div>
    ),
  ],
  beforeEach: installPackageMocks,
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Selects packages and schedules their action immediately or through an action chain. One representative PTF is preselected so the confirmation scheduler can be reached directly; all list, selection, maintenance-window, and scheduling requests stay local to Storybook.",
      },
    },
  },
  args: {
    serverId: 1000010001,
    selectionSet: "storybook_ptf_install",
    actionChains: [
      { id: 12, text: "Monthly maintenance" },
      { id: 18, text: "Urgent production fixes" },
    ],
    icon: "fa-fire-extinguisher",
    listDataAPI: "/storybook/packages",
    scheduleActionAPI: "/storybook/packages/scheduleAction",
    actionType: "packages.update",
    listTitle: "Install Program Temporary Fixes (PTFs)",
    listSummary:
      "Select the support-provided PTF packages that should be installed on this system, then review the schedule.",
    listEmptyText: "No Program Temporary Fixes (PTFs) available.",
    listActionLabel: "Install PTFs",
    listColumns: packageColumns,
    confirmTitle: "Confirm Program Temporary Fixes (PTFs) Installation",
  },
  argTypes: {
    serverId: {
      control: "number",
      description: "Target system identifier used for maintenance-window checks.",
    },
    selectionSet: {
      control: false,
      description: "Server-side selection-set label; requests are intercepted by this story.",
    },
    actionChains: {
      control: false,
      description: "Existing action chains offered by the confirmation scheduler.",
    },
    icon: {
      control: "text",
      description: "Icon shown in the package-list panel heading.",
    },
    listDataAPI: {
      control: false,
      description: "Paged package endpoint; served by local fixtures in this story.",
    },
    scheduleActionAPI: {
      control: false,
      description: "Scheduling endpoint; served locally in this story.",
    },
    actionType: {
      control: false,
      description: "Action type used when checking maintenance windows and scheduling.",
    },
    listTitle: {
      control: "text",
      description: "Title of the package-selection step.",
    },
    listSummary: {
      control: "text",
      description: "Guidance shown above the available packages.",
    },
    listEmptyText: {
      control: "text",
      description: "Message shown when the package endpoint is empty.",
    },
    listActionLabel: {
      control: "text",
      description: "Label of the button that advances to confirmation.",
    },
    listColumns: {
      control: false,
      description: "Additional columns rendered beside the package name.",
    },
    confirmTitle: {
      control: "text",
      description: "Title of the scheduling confirmation step.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1400px", minHeight: "850px", padding: "24px" }}>
      <PackageListActionScheduler key={`${args.serverId}-${args.actionType}`} {...args} />
    </div>
  ),
} satisfies Meta<typeof PackageListActionScheduler>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
