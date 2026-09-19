import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Highlight } from "./Highlight";

const meta = {
  title: "Components/Table/Highlight",
  component: Highlight,
  parameters: {
    docs: {
      description: {
        component:
          "Highlights the first case-insensitive occurrence of a search string inside text, typically in filtered table cells.",
      },
    },
  },
  args: {
    text: "Production server group",
    highlight: "server",
    enabled: true,
    className: "",
  },
  argTypes: {
    text: {
      control: "text",
      description: "Complete text to display.",
      table: { type: { summary: "string" } },
    },
    highlight: {
      control: "text",
      description: "Case-insensitive substring to mark within `text`.",
      table: { type: { summary: "string" } },
    },
    enabled: {
      control: "boolean",
      description: "Enables substring highlighting. Disabled text is rendered unchanged.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the outer span.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof Highlight>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const NoMatch: Story = {
  args: {
    highlight: "missing",
  },
};

export const Disabled: Story = {
  args: {
    enabled: false,
  },
};
