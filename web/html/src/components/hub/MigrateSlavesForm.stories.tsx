import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Utils } from "utils/functions";
import Network from "utils/network";

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

const meta = {
  title: "Compositions/Hub/MigrateSlavesForm",
  component: MigrateSlavesForm,
  beforeEach: () => {
    const originalNetworkPost = Network.post;
    Network.post = mockNetworkPost;

    return () => {
      if (Network.post === mockNetworkPost) {
        Network.post = originalNetworkPost;
      }
    };
  },
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Migrates legacy ISS slaves to peripheral servers. The representative entries cover a ready server, an optional unselected server that still needs a token, and a disabled server. Submitting the selected entry opens a locally mocked partial-result dialog.",
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
      <MigrateSlavesForm key={args.migrateFrom} {...args} />
    </div>
  ),
} satisfies Meta<typeof MigrateSlavesForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
