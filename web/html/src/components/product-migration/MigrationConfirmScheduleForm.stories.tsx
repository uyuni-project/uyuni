import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { MigrationConfirmScheduleForm } from "./MigrationConfirmScheduleForm";
import { migrationSource, migrationTargetProduct, selectedChannelTree, systemsData } from "./story-fixtures";

const mockNetworkPost = (() =>
  Utils.cancelable(
    Promise.resolve({
      data: {
        maintenanceWindowsMultiSchedules: false,
        maintenanceWindows: null,
      },
    })
  )) as typeof Network.post;

const meta = {
  title: "Compositions/Product Migration/MigrationConfirmScheduleForm",
  component: MigrationConfirmScheduleForm,
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
          "Reviews the target product and channels, then schedules either a migration dry run or the migration itself. The fixture also demonstrates the SLES 15 to SLES 16 pre-flight guidance.",
      },
    },
  },
  args: {
    systemsData,
    actionChains: [
      { id: 11, text: "Monthly maintenance" },
      { id: 12, text: "Production rollout" },
    ],
    migrationSource,
    migrationTarget: migrationTargetProduct,
    migrationChannels: selectedChannelTree,
    allowVendorChange: false,
    hasDryRunCapability: true,
    onBack: action("back requested"),
    onConfirm: async (dryRun, earliest, actionChain) =>
      action("migration confirmed")({
        dryRun,
        earliest: earliest.toISOString(),
        actionChain: actionChain?.text,
      }),
  },
  argTypes: {
    systemsData: {
      control: false,
      description: "Systems included in the migration.",
    },
    actionChains: {
      control: false,
      description: "Action chains available as an alternative to date-based scheduling.",
    },
    migrationSource: {
      control: false,
      description: "Source product used to determine whether migration-specific guidance is required.",
    },
    migrationTarget: {
      control: false,
      description: "Product that will be installed after migration.",
    },
    migrationChannels: {
      control: false,
      description: "Base and child channels selected for the target product.",
    },
    allowVendorChange: {
      control: "boolean",
      description: "Whether changing the package vendor is allowed during migration.",
    },
    hasDryRunCapability: {
      control: "boolean",
      description: "Enables the recommended dry-run action when supported by all target systems.",
    },
    onBack: {
      control: false,
      description: "Returns to channel selection.",
    },
    onConfirm: {
      control: false,
      description: "Schedules a dry run or migration using the selected date or action chain.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1200px", minHeight: "900px", padding: "24px" }}>
      <MigrationConfirmScheduleForm {...args} />
    </div>
  ),
} satisfies Meta<typeof MigrationConfirmScheduleForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
