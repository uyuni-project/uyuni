import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { ExampleRow, StripedExampleSection } from "components/example-layout";

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
    },
    htmlFor: {
      control: "text",
      description: "Identifier of the form control labelled by this element.",
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the label.",
    },
    required: {
      control: "boolean",
      description: "Adds the required-field asterisk before the trailing colon.",
    },
  },
} satisfies Meta<typeof Label>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedExampleSection>
      <ExampleRow>
        <Label name="Optional field" />
        <Label name="Required field" required />
      </ExampleRow>
    </StripedExampleSection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Optional and required label states." } },
  },
};
