import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { DEPRECATED_Select } from "./DEPRECATED_Select";

type DEPRECATED_SelectProps = React.ComponentProps<typeof DEPRECATED_Select>;

const StatefulSelectInForm = (args: DEPRECATED_SelectProps & { options: any[] }) => {
  const [model, setModel] = useState({ [args.name || "select"]: args.defaultValue || "" });

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        setModel({ ...newModel });
        action("form changed")(newModel);
      }}
      onSubmit={() => action("form submitted")(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <DEPRECATED_Select {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const meta = {
  title: "Components/DEPRECATED/DEPRECATED_Select",
  component: DEPRECATED_Select,
  decorators: [
    (Story) => (
      <div style={{ minHeight: "400px", maxWidth: "600px" }}>
        <Story />
      </div>
    ),
  ],
  parameters: {
    docs: {
      description: {
        component: `
**⚠️ DEPRECATED - Do not use in new code**

This component is deprecated. Use one of the following instead:
- For standalone dropdowns: \`import { Select } from "components/input"\`
- For Formik forms: \`<Field as={Field.Select} />\`

This component uses the old form context and will be removed in a future release.
        `,
      },
    },
  },
  args: {
    name: "country",
    label: "Country",
    placeholder: "Select a country",
    options: [
      { value: "us", label: "United States" },
      { value: "uk", label: "United Kingdom" },
      { value: "ca", label: "Canada" },
      { value: "de", label: "Germany" },
      { value: "fr", label: "France" },
    ],
    isClearable: false,
    labelClass: "col-md-3",
    divClass: "col-md-6",
  },
  argTypes: {
    name: {
      control: "text",
      description: "Name of the field in the form model.",
      table: { type: { summary: "string" } },
    },
    label: {
      control: "text",
      description: "Label text for the select.",
      table: { type: { summary: "string" } },
    },
    placeholder: {
      control: "text",
      description: "Placeholder text when no option is selected.",
      table: { type: { summary: "ReactNode" } },
    },
    options: {
      control: "object",
      description: "Array of options with value and label properties.",
      table: { type: { summary: "Array<{value: string, label: string}>" } },
    },
    isClearable: {
      control: "boolean",
      description: "Allow clearing the selected value.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    isMulti: {
      control: "boolean",
      description: "Allow selecting multiple values.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Disable the select.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
  },
  render: (args) => <StatefulSelectInForm {...args} />,
} satisfies Meta<DEPRECATED_SelectProps & { options: any[] }>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Interactive select dropdown. Use Select component for new code.",
      },
    },
  },
};

export const Clearable: Story = {
  args: {
    name: "priority",
    label: "Priority",
    placeholder: "Select priority",
    options: [
      { value: "high", label: "High" },
      { value: "medium", label: "Medium" },
      { value: "low", label: "Low" },
    ],
    isClearable: true,
  },
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Select with clearable option. Click the X to clear selection.",
      },
    },
  },
};

export const MultiSelect: Story = {
  args: {
    name: "tags",
    label: "Tags",
    placeholder: "Select tags",
    options: [
      { value: "bug", label: "Bug" },
      { value: "feature", label: "Feature" },
      { value: "enhancement", label: "Enhancement" },
      { value: "documentation", label: "Documentation" },
    ],
    isMulti: true,
  },
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Multi-select dropdown. Select multiple options.",
      },
    },
  },
};

export const WithDefaultValue: Story = {
  args: {
    name: "language",
    label: "Language",
    defaultValue: "en",
    options: [
      { value: "en", label: "English" },
      { value: "es", label: "Spanish" },
      { value: "fr", label: "French" },
      { value: "de", label: "German" },
    ],
  },
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Select with a default value pre-selected.",
      },
    },
  },
};
