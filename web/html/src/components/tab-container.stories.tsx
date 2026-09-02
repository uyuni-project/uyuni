import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { TabContainer } from "./tab-container";

const tabHashes = ["#overview", "#systems", "#activity"];

const meta = {
  title: "Components/Navigation/TabContainer",
  component: TabContainer,
  parameters: {
    docs: {
      description: {
        component:
          "Hash-based tab container that renders matching label and content arrays and reports tab changes to its parent.",
      },
    },
  },
  args: {
    labels: ["Overview", "Systems", "Activity"],
    hashes: tabHashes,
    tabs: [
      <p key="overview">Overview content</p>,
      <p key="systems">Systems content</p>,
      <p key="activity">Activity content</p>,
    ],
    initialActiveTabHash: "#overview",
    onTabHashChange: action("tab changed"),
  },
  argTypes: {
    labels: {
      control: "object",
      description: "Ordered labels for the tabs. Use `null` at an index to hide that label.",
      table: { type: { summary: "ReactNode[]" } },
    },
    hashes: {
      control: "object",
      description: "Ordered URL hashes corresponding to `labels` and `tabs`; each value must begin with `#`.",
      table: { type: { summary: "string[]" } },
    },
    tabs: {
      control: false,
      description: "Ordered content nodes corresponding to the hash at the same index.",
      table: { type: { summary: "ReactNode[]" } },
    },
    initialActiveTabHash: {
      control: "select",
      options: tabHashes,
      description: "Hash selected when the component is initialized or receives updated props.",
      table: { type: { summary: "string" } },
    },
    onTabHashChange: {
      action: "tab changed",
      description: "Called with the newly selected hash.",
      table: { type: { summary: "(hash: string) => any" } },
    },
  },
} satisfies Meta<typeof TabContainer>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const InitiallyActive: Story = {
  args: {
    initialActiveTabHash: "#systems",
  },
};
