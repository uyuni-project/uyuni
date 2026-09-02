import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { FormGroup } from "./FormGroup";
import { Label } from "./Label";

const meta = {
  title: "Components/Inputs/FormGroup",
  component: FormGroup,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component: "Layout wrapper for one Uyuni form row, with an optional validation-error state.",
      },
    },
  },
  args: {
    isError: false,
    className: "",
    children: "Form field content",
  },
  argTypes: {
    isError: {
      control: "boolean",
      description: "Adds the Bootstrap `has-error` class when enabled.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the row.",
      table: { type: { summary: "string" } },
    },
    children: {
      control: "text",
      description: "Form controls and supporting content rendered inside the row.",
      table: { type: { summary: "ReactNode" } },
    },
  },
} satisfies Meta<typeof FormGroup>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const WithField: Story = {
  render: (args) => (
    <FormGroup {...args}>
      <Label name="System name" htmlFor="form-group-system-name" required className="col-md-3" />
      <div className="col-md-6">
        <input id="form-group-system-name" className="form-control" defaultValue="example.example.org" />
      </div>
    </FormGroup>
  ),
  args: {
    children: undefined,
  },
  parameters: {
    docs: { description: { story: "A representative horizontal form row composed with `Label`." } },
  },
};
