import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { MigrationTargetSelectorForm } from "./MigrationTargetSelectorForm";
import { migrationSource, migrationTargets } from "./story-fixtures";

const meta = {
  title: "Compositions/Product Migration/MigrationTargetSelectorForm",
  component: MigrationTargetSelectorForm,
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Selects the destination product for a product migration. The representative data includes one available target and one unavailable target with missing channels.",
      },
    },
  },
  args: {
    migrationSource,
    migrationTargets,
    targetId: migrationTargets[0].id,
    onTargetChange: async (targetId) => action("target selected")(targetId),
  },
  argTypes: {
    migrationSource: {
      control: false,
      description: "Product currently installed on the selected systems.",
    },
    migrationTargets: {
      control: false,
      description: "Eligible targets, including missing-channel information used to disable unavailable choices.",
    },
    targetId: {
      control: "select",
      options: migrationTargets.map((target) => target.id),
      description: "Initially selected target identifier.",
    },
    onTargetChange: {
      control: false,
      description: "Persists the selected target and advances to channel selection.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1200px", minHeight: "520px", padding: "24px" }}>
      <MigrationTargetSelectorForm key={args.targetId} {...args} />
    </div>
  ),
} satisfies Meta<typeof MigrationTargetSelectorForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
