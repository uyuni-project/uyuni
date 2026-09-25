import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { RankingTable } from "./ranking-table";

const rankedChannels = [
  {
    label: "highstate",
    name: "Highstate",
    type: "state",
    position: 1,
  },
  {
    label: "security-baseline",
    name: "Security baseline",
    type: "internal_state",
    position: 2,
  },
  {
    label: "application-config",
    name: "Application configuration",
    type: "normal",
    position: 3,
  },
];

const meta = {
  title: "Components/Data Display/RankingTable",
  component: RankingTable,
  parameters: {
    docs: {
      description: {
        component:
          "Sortable ranking list used for configuration channels and Salt states. Drag rows to change their priority; onUpdate receives the reordered items with one-based positions.",
      },
    },
  },
  args: {
    items: rankedChannels,
    emptyMsg: "There are no channels to rank.",
    onUpdate: action("ranking updated"),
  },
  argTypes: {
    items: {
      control: false,
      description: "Items containing label, name, type, and optional one-based position fields.",
      table: { type: { summary: "RankingItem[]" } },
    },
    emptyMsg: {
      control: "text",
      description: "Message displayed when the ranking contains no items.",
    },
    onUpdate: {
      action: "ranking updated",
      description: "Called with cloned items after their positions have been updated.",
      table: { type: { summary: "(items: RankingItem[]) => any" } },
    },
  },
} satisfies Meta<typeof RankingTable>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Empty: Story = {
  args: {
    items: [],
  },
};
