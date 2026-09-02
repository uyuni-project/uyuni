import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { CustomDiv } from "./custom-objects";

const meta = {
  title: "Components/Layout/CustomDiv",
  component: CustomDiv,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component: "A small layout wrapper that combines a numeric width and CSS unit into an inline width value.",
      },
    },
  },
  args: {
    width: "50",
    um: "%",
    className: "",
    title: "Custom-width content",
    children: "Content inside a custom-width container",
  },
  argTypes: {
    width: {
      control: "text",
      description: "Numeric portion of the generated CSS width.",
      table: { type: { summary: "string" } },
    },
    um: {
      control: "select",
      options: ["px", "%", "rem", "em", "vw"],
      description: "CSS unit appended to `width`.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the wrapper.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Optional native tooltip text for the wrapper.",
      table: { type: { summary: "string" } },
    },
    children: {
      control: "text",
      description: "Content rendered inside the wrapper.",
      table: { type: { summary: "ReactNode" } },
    },
  },
} satisfies Meta<typeof CustomDiv>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
