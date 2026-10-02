import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { MigrationChannelsSelectorForm } from "./MigrationChannelsSelectorForm";
import {
  baseChannelTrees,
  mandatoryMap,
  migrationSource,
  migrationTargets,
  reversedMandatoryMap,
} from "./story-fixtures";

const meta = {
  title: "Compositions/Product Migration/MigrationChannelsSelectorForm",
  component: MigrationChannelsSelectorForm,
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Selects the target base and child channels for a product migration. Mandatory relationships are represented so selecting or removing a channel also updates its dependencies.",
      },
    },
  },
  args: {
    migrationSource,
    migrationTarget: migrationTargets[0],
    baseChannelTrees,
    mandatoryMap,
    reversedMandatoryMap,
    allowVendorChange: false,
    onChannelSelection: (channelTree, allowVendorChange) =>
      action("channels selected")({ channelTree, allowVendorChange }),
    onBack: action("back requested"),
  },
  argTypes: {
    migrationSource: {
      control: false,
      description: "Product currently installed on the selected systems.",
    },
    migrationTarget: {
      control: false,
      description: "Target selected in the preceding migration step.",
    },
    baseChannelTrees: {
      control: false,
      description: "Available base channels and their child channels.",
    },
    mandatoryMap: {
      control: false,
      description: "Channels required by each base or child channel.",
    },
    reversedMandatoryMap: {
      control: false,
      description: "Reverse dependency mapping used when a required channel is removed.",
    },
    baseChannel: {
      control: false,
      description: "Previously selected base channel, when returning to this step.",
    },
    childChannels: {
      control: false,
      description: "Previously selected child channels, when returning to this step.",
    },
    allowVendorChange: {
      control: "boolean",
      description: "Initial vendor-change selection.",
    },
    onChannelSelection: {
      control: false,
      description: "Receives the normalized channel tree and vendor-change choice.",
    },
    onBack: {
      control: false,
      description: "Returns to target selection.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1200px", minHeight: "760px", padding: "24px" }}>
      <MigrationChannelsSelectorForm key={String(args.allowVendorChange)} {...args} />
    </div>
  ),
} satisfies Meta<typeof MigrationChannelsSelectorForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
