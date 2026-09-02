import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { ItemsPerPageSelector } from "./pagination";

type ItemsPerPageSelectorProps = React.ComponentProps<typeof ItemsPerPageSelector>;

const StatefulItemsPerPageSelector = (props: ItemsPerPageSelectorProps) => {
  const [currentValue, setCurrentValue] = useState(props.currentValue);

  useEffect(() => setCurrentValue(props.currentValue), [props.currentValue]);

  return (
    <ItemsPerPageSelector
      {...props}
      currentValue={currentValue}
      onChange={(value) => {
        setCurrentValue(value);
        props.onChange(value);
      }}
    />
  );
};

const meta = {
  title: "Components/Navigation/ItemsPerPageSelector",
  component: ItemsPerPageSelector,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component: "Dropdown that reports the visible item range and lets the user choose a standard page size.",
      },
    },
  },
  args: {
    currentValue: 15,
    itemCount: 128,
    fromItem: 1,
    toItem: 15,
    onChange: action("page size changed"),
  },
  argTypes: {
    currentValue: {
      control: "select",
      options: [5, 10, 15, 25, 50, 100, 250, 500],
      description: "Currently selected number of items per page.",
      table: { type: { summary: "number" } },
    },
    itemCount: {
      control: { type: "number", min: 0, step: 1 },
      description: "Total number of items in the result set.",
      table: { type: { summary: "number" } },
    },
    fromItem: {
      control: { type: "number", min: 0, step: 1 },
      description: "One-based number of the first visible item.",
      table: { type: { summary: "number" } },
    },
    toItem: {
      control: { type: "number", min: 0, step: 1 },
      description: "One-based number of the last visible item.",
      table: { type: { summary: "number" } },
    },
    onChange: {
      action: "page size changed",
      description: "Called with the selected page size.",
      table: { type: { summary: "(value: number) => any" } },
    },
  },
  render: (args) => <StatefulItemsPerPageSelector {...args} />,
} satisfies Meta<typeof ItemsPerPageSelector>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const LaterPage: Story = {
  args: {
    fromItem: 46,
    toItem: 60,
  },
};
