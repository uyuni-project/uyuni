/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Utils } from "utils/functions";
import Network from "utils/network";
import { mockNetwork } from "utils/storybook/mock-network";

import { MigrateSlavesForm } from "./MigrateSlavesForm";
import { migrationEntries } from "./story-fixtures";
import { MigrationMessageLevel, MigrationResultCode, MigrationVersion } from "./types";

const mockNetworkPost = (() =>
  Utils.cancelable(
    Promise.resolve({
      resultCode: MigrationResultCode.PARTIAL,
      messageSet: [
        {
          severity: MigrationMessageLevel.INFO,
          message: "legacy-east.example.com was migrated successfully.",
        },
        {
          severity: MigrationMessageLevel.ERROR,
          message: "legacy-west.example.com could not be reached.",
        },
      ],
    })
  )) as typeof Network.post;

const StatefulMigration = (args: React.ComponentProps<typeof MigrateSlavesForm>) => {
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const original = window.pageRenderers;
    const replacement = {
      ...original,
      spaengine: {
        ...original?.spaengine,
        navigate: (url: string) => {
          action("navigated")(url);
          setRevision((current) => current + 1);
        },
      },
    };
    window.pageRenderers = replacement;
    return () => {
      if (window.pageRenderers === replacement) {
        window.pageRenderers = original;
      }
    };
  }, []);

  return <MigrateSlavesForm key={`${args.migrateFrom}-${revision}`} {...args} />;
};

const meta = {
  title: "Compositions/Hub/MigrateSlavesForm",
  component: MigrateSlavesForm,
  beforeEach: () => mockNetwork({ post: mockNetworkPost }),
  parameters: {
    layout: "fullscreen",
    docs: {
      story: {
        inline: false,
        height: "900px",
      },
      description: {
        component:
          "Migrates legacy ISS slaves to peripheral servers. The representative entries cover a ready server, an optional unselected server that still needs a token, and a disabled server. Submitting the selected entry opens a locally mocked partial-result dialog. Close and Cancel log navigation and restart the example.",
      },
    },
  },
  args: {
    title: "Migrate legacy servers",
    migrationEntries,
    migrateFrom: MigrationVersion.v1,
  },
  argTypes: {
    title: {
      control: "text",
      description: "Heading displayed above the migration workflow.",
    },
    migrationEntries: {
      control: false,
      description: "Legacy servers and their current migration readiness.",
    },
    migrateFrom: {
      control: "select",
      options: [MigrationVersion.v1, MigrationVersion.v2],
      description: "Legacy ISS version; version 2 also enables adding servers manually.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1400px", minHeight: "800px", padding: "24px" }}>
      <StatefulMigration {...args} />
    </div>
  ),
} satisfies Meta<typeof MigrateSlavesForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
