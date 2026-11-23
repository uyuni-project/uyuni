/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { TopPanel } from "./TopPanel";

const meta = {
  title: "Components/Panels/TopPanel",
  component: TopPanel,
  parameters: {
    docs: {
      description: {
        component:
          "Page-level heading with an optional icon, documentation link, right-aligned action, and content below the toolbar.",
      },
    },
  },
  args: {
    title: "Systems",
    icon: "fa-server",
    helpUrl: "reference/systems/system-details.html",
    button: <Button className="btn-primary" icon="fa-plus" text="Create system" handler={action("create clicked")} />,
    children: <p>Page content begins below the top panel.</p>,
  },
  argTypes: {
    title: { control: "text", description: "Translated page heading." },
    icon: {
      control: "text",
      description: "Font Awesome icon class displayed before the title. Ignored when `iconType` is set.",
    },
    iconType: {
      control: "select",
      options: [undefined, "header-system", "header-package", "header-schedule", "header-channel", "header-user"],
      description:
        "IconTag type (for example `header-system`) displayed before the title. Takes precedence over `icon`, so pages use the same header icons as the rest of the UI.",
    },
    helpUrl: { control: "text", description: "Relative documentation URL used by HelpLink." },
    button: { control: false, description: "Optional action rendered on the right side of the heading." },
    children: { control: false, description: "Page content rendered below the heading." },
  },
} satisfies Meta<typeof TopPanel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const WithIconType: Story = {
  args: {
    icon: undefined,
    iconType: "header-system",
    helpUrl: "reference/systems/systems-list.html",
  },
  parameters: {
    docs: {
      description: {
        story: "Uses `iconType` to render the icon through `IconTag`, as the Systems list page does.",
      },
    },
  },
};

export const WithFontAwesomeIcon: Story = {
  args: {
    icon: "fa-search",
    iconType: undefined,
    title: "CVE Audit",
    helpUrl: "reference/audit/audit-cve-audit.html",
    button: undefined,
  },
  parameters: {
    docs: {
      description: {
        story: "Uses `icon` with a Font Awesome class, for icons that have no `IconTag` type.",
      },
    },
  },
};

export const IconTypeTakesPrecedence: Story = {
  args: {
    icon: "fa-server",
    iconType: "header-package",
    title: "Packages",
  },
  parameters: {
    docs: {
      description: {
        story: "When both are set, only `iconType` is rendered and `icon` is ignored.",
      },
    },
  },
};

export const WithoutIcon: Story = {
  args: {
    icon: undefined,
    iconType: undefined,
    title: "Settings",
    helpUrl: undefined,
    button: undefined,
  },
  parameters: {
    docs: {
      description: {
        story: "Without `icon` or `iconType`, no icon is rendered.",
      },
    },
  },
};
