import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SelectSearchField } from "./SelectSearchField";

const options = [
  { label: "Active", value: "active" },
  { label: "Inactive", value: "inactive" },
  { label: "Unknown", value: "unknown" },
];

const meta = {
  title: "Components/Table/SelectSearchField",
  component: SelectSearchField,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Select-based table filter that prepends an `All` option and converts that selection to an empty search criterion.",
      },
    },
  },
  args: {
    label: "Status",
    criteria: "active",
    options,
    onSearch: action("criteria changed"),
  },
  argTypes: {
    label: {
      control: "text",
      description: "Placeholder shown by the select when it has no value.",
      table: { type: { summary: "string" } },
    },
    criteria: {
      control: "select",
      options: ["", ...options.map((option) => option.value)],
      description: "Currently selected filter value. An empty string represents all values.",
      table: { type: { summary: "string" } },
    },
    options: {
      control: "object",
      description: "Selectable filter values; the component adds the `All` option automatically.",
      table: { type: { summary: "{ label: string; value: string }[]" } },
    },
    onSearch: {
      action: "criteria changed",
      description: "Called with the selected value, or an empty string when `All` is selected.",
      table: { type: { summary: "(criteria: string) => void" } },
    },
  },
  render: (args) => <SelectSearchField key={args.criteria} {...args} />,
} satisfies Meta<typeof SelectSearchField>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const All: Story = {
  args: {
    criteria: "",
  },
};
