import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { TextField } from "./fields";

type TextFieldProps = React.ComponentProps<typeof TextField>;

const ControlledTextField = (props: TextFieldProps) => {
  const [value, setValue] = useState(props.value ?? "");

  useEffect(() => setValue(props.value ?? ""), [props.value]);

  return (
    <TextField
      {...props}
      value={value}
      onChange={(event) => {
        setValue(event.target.value);
        props.onChange?.(event);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/TextField",
  component: TextField,
  parameters: {
    docs: {
      description: {
        component: "A lightweight controlled text input that can run a dedicated callback when the user presses Enter.",
      },
    },
  },
  args: {
    id: "storybook-text-field",
    value: "Search term",
    placeholder: "Type and press Enter",
    className: "form-control",
  },
  argTypes: {
    id: {
      control: "text",
      description: "HTML identifier assigned to the input.",
      table: { type: { summary: "string" } },
    },
    value: {
      control: "text",
      description: "Current controlled value of the input.",
      table: { type: { summary: "string | number | readonly string[]" } },
    },
    placeholder: {
      control: "text",
      description: "Hint displayed while the input is empty.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "CSS classes for the input. Defaults to `form-control`.",
      table: { type: { summary: "string" }, defaultValue: { summary: "form-control" } },
    },
    onChange: {
      action: "changed",
      description: "Called with the input change event whenever its value changes.",
      table: { type: { summary: "ChangeEventHandler<HTMLInputElement>" } },
    },
    onPressEnter: {
      action: "enter pressed",
      description: "Called with the keyboard event when Enter is pressed.",
      table: { type: { summary: "(event: KeyboardEvent<HTMLInputElement>) => void" } },
    },
  },
  render: (args) => <ControlledTextField {...args} />,
} satisfies Meta<typeof TextField>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Empty: Story = {
  args: {
    value: "",
    placeholder: "Search systems",
  },
};
