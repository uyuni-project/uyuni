import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { CheckInput } from "./CheckInput";

type CheckInputProps = React.ComponentProps<typeof CheckInput>;

const ControlledCheckInput = (props: CheckInputProps) => {
  const [checked, setChecked] = useState(props.checked ?? false);

  useEffect(() => setChecked(props.checked ?? false), [props.checked]);

  return (
    <CheckInput
      {...props}
      checked={checked}
      onChange={(nextChecked) => {
        setChecked(nextChecked);
        props.onChange?.(nextChecked);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/CheckInput",
  component: CheckInput,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Low-level checkbox input with support for the DOM-only indeterminate state. Use `Check` when the standard Uyuni label wrapper is also needed.",
      },
    },
  },
  args: {
    "aria-label": "Select item",
    checked: false,
    disabled: false,
    indeterminate: false,
  },
  argTypes: {
    indeterminate: {
      control: "boolean",
      description: "Sets the checkbox's DOM `indeterminate` property.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    checked: {
      control: "boolean",
      description: "Current checked state of the checkbox.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents the checkbox from being changed.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    onChange: {
      action: "changed",
      description: "Called with the checkbox's next checked value.",
      table: { type: { summary: "(checked: boolean) => void" } },
    },
  },
  render: (args) => <ControlledCheckInput {...args} />,
} satisfies Meta<typeof CheckInput>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <CheckInput aria-label="Unchecked" />
        <CheckInput aria-label="Checked" defaultChecked />
        <CheckInput aria-label="Indeterminate" indeterminate />
        <CheckInput aria-label="Disabled" disabled />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Unchecked, checked, indeterminate, and disabled native states." } },
  },
};
