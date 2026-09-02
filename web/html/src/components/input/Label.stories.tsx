import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Label } from "./Label";

const meta = {
  title: "Components/Inputs/Label",
  component: Label,
  parameters: {
    docs: {
      description: {
        component:
          "Uyuni form label that consistently renders the trailing colon and required-field marker used by form controls.",
      },
    },
  },
  args: {
    name: "System name",
    htmlFor: "system-name",
    className: "",
    required: false,
  },
  argTypes: {
    name: {
      control: "text",
      description: "Visible label text.",
      table: { type: { summary: "string" } },
    },
    htmlFor: {
      control: "text",
      description: "Identifier of the form control labelled by this element.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the label.",
      table: { type: { summary: "string" } },
    },
    required: {
      control: "boolean",
      description: "Adds the required-field asterisk before the trailing colon.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
  },
} satisfies Meta<typeof Label>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Label name="Optional field" />
        <Label name="Required field" required />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Optional and required label states." } },
  },
};
