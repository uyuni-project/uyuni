import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { NumericSearchField } from "./NumericSearchField";

const meta = {
  title: "Components/Table/NumericSearchField",
  component: NumericSearchField,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Numeric table filter composed of a comparison matcher and number input. It reports criteria such as `>=10`.",
      },
    },
  },
  args: {
    name: "package-count",
    criteria: ">=10",
    onSearch: action("criteria changed"),
  },
  argTypes: {
    name: {
      control: "text",
      description: "HTML name assigned to the numeric input.",
      table: { type: { summary: "string" } },
    },
    criteria: {
      control: "text",
      description: "Current matcher and numeric value encoded as one string, for example `>=10`.",
      table: { type: { summary: "string" } },
    },
    onSearch: {
      action: "criteria changed",
      description: "Called with the combined matcher and value, or `null` while either part is empty.",
      table: { type: { summary: "(criteria: string | null) => void" } },
    },
  },
  render: (args) => <NumericSearchField key={args.criteria} {...args} />,
} satisfies Meta<typeof NumericSearchField>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Empty: Story = {
  args: {
    criteria: "",
  },
};
