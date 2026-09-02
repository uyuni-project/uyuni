import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Toggler } from "./toggler";

type TogglerProps = React.ComponentProps<typeof Toggler>;

const ControlledToggler = (props: TogglerProps) => {
  const [value, setValue] = useState(props.value ?? false);

  useEffect(() => setValue(props.value ?? false), [props.value]);

  return (
    <Toggler
      {...props}
      value={value}
      handler={(nextValue) => {
        setValue(nextValue);
        props.handler(nextValue);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/Toggler",
  component: Toggler,
  parameters: {
    docs: {
      description: {
        component:
          "A compact boolean toggle. It reports the inverse of its current value when activated; the parent owns the value.",
      },
    },
  },
  args: {
    handler: () => undefined,
    text: "Automatic updates",
    value: true,
    disabled: false,
    className: "",
  },
  argTypes: {
    handler: {
      action: "toggled",
      description: "Called with the next boolean value when the toggle is activated.",
      table: { type: { summary: "(value: boolean) => void" } },
    },
    text: {
      control: "text",
      description: "Label displayed alongside the toggle icon.",
      table: { type: { summary: "ReactNode" } },
    },
    value: {
      control: "boolean",
      description: "Current boolean value represented by the toggle.",
      table: { type: { summary: "boolean" } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents interaction and applies muted styling.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the outer element.",
      table: { type: { summary: "string" } },
    },
  },
  render: (args) => <ControlledToggler {...args} />,
} satisfies Meta<typeof Toggler>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Toggler value text="Enabled" handler={() => undefined} />
        <Toggler value={false} text="Disabled value" handler={() => undefined} />
        <Toggler value text="Unavailable" disabled handler={() => undefined} />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Enabled, disabled-value, and unavailable states." } },
  },
};
