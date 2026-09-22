import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Range } from "./Range";

type RangeProps = React.ComponentProps<typeof Range>;

const StatefulRangeInForm = (args: RangeProps) => {
  const [model, setModel] = useState({
    [`${args.prefix}_start`]: args.defaultStart || "",
    [`${args.prefix}_end`]: args.defaultEnd || "",
  });

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        setModel(newModel);
        action("form changed")(newModel);
      }}
      onSubmit={() => {
        action("form submitted")(model);
      }}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Range {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const meta = {
  title: "Components/Inputs/Range",
  component: Range,
  parameters: {
    docs: {
      description: {
        component:
          "Range input component that provides two text fields (start and end) separated by a hyphen. Integrates with the Uyuni form system using a prefix for field names (`prefix_start` and `prefix_end`).",
      },
    },
  },
  args: {
    prefix: "value",
    label: "Value range",
    defaultStart: "",
    defaultEnd: "",
    placeholder: "",
    required: false,
    disabled: false,
    labelClass: "col-md-3",
    divClass: "col-md-6",
  },
  argTypes: {
    prefix: {
      control: "text",
      description:
        "Name prefix for the two fields. Creates `{prefix}_start` and `{prefix}_end` fields in the form model.",
      table: { type: { summary: "string" } },
    },
    label: {
      control: "text",
      description: "Label text displayed for the range input group.",
      table: { type: { summary: "string" } },
    },
    defaultStart: {
      control: "text",
      description: "Default value for the start field if not set in the form model.",
      table: { type: { summary: "string" } },
    },
    defaultEnd: {
      control: "text",
      description: "Default value for the end field if not set in the form model.",
      table: { type: { summary: "string" } },
    },
    placeholder: {
      control: "text",
      description: "Placeholder text for both start and end inputs.",
      table: { type: { summary: "string" } },
    },
    required: {
      control: "boolean",
      description: "Marks both fields as required. Shows validation errors when empty.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables both input fields.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    hint: {
      control: "text",
      description: "Help text displayed below the inputs.",
      table: { type: { summary: "ReactNode" } },
    },
    invalidHint: {
      control: "text",
      description: "Error message shown when validation fails.",
      table: { type: { summary: "ReactNode" } },
    },
    validators: {
      control: false,
      description:
        "Array of validation functions. Receives an object with both values: `{ prefix_start: string, prefix_end: string }`.",
      table: { type: { summary: "((values: object) => boolean | Promise<boolean>)[]" } },
    },
    labelClass: {
      control: "text",
      description: "CSS classes applied to the label element.",
      table: { type: { summary: "string" } },
    },
    divClass: {
      control: "text",
      description: "CSS classes applied to the wrapper div containing the inputs and hints.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "CSS classes applied to the form group wrapper.",
      table: { type: { summary: "string" } },
    },
    inputClass: {
      control: "text",
      description: "CSS classes applied directly to both input elements.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "HTML title attribute for the input elements (suffixed with 'start' and 'end').",
      table: { type: { summary: "string" } },
    },
  },
  render: (args) => <StatefulRangeInForm {...args} />,
} satisfies Meta<typeof Range>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive range input with two text fields for start and end values.",
      },
    },
  },
};

export const NumericRange: Story = {
  args: {
    prefix: "port",
    label: "Port range",
    defaultStart: "8000",
    defaultEnd: "9000",
    placeholder: "Port number",
    hint: "Enter the start and end ports for the range",
  },
  parameters: {
    docs: {
      description: {
        story: "Range input for numeric values like port numbers.",
      },
    },
  },
};

export const DateRange: Story = {
  args: {
    prefix: "date",
    label: "Date range",
    defaultStart: "2024-01-01",
    defaultEnd: "2024-12-31",
    placeholder: "YYYY-MM-DD",
    hint: "Enter start and end dates",
  },
  parameters: {
    docs: {
      description: {
        story: "Range input for date values.",
      },
    },
  },
};

export const Required: Story = {
  args: {
    prefix: "score",
    label: "Score range",
    required: true,
    invalidHint: "Both start and end values are required",
    hint: "Enter minimum and maximum scores",
  },
  parameters: {
    docs: {
      description: {
        story: "Required range input. Try submitting without filling both fields to see validation.",
      },
    },
  },
};

export const WithValidation: Story = {
  args: {
    prefix: "age",
    label: "Age range",
    defaultStart: "18",
    defaultEnd: "65",
    required: true,
    invalidHint: "End value must be greater than or equal to start value",
    validators: [
      (values: any) => {
        const start = parseInt(values.age_start || "0", 10);
        const end = parseInt(values.age_end || "0", 10);
        return end >= start;
      },
    ],
  },
  parameters: {
    docs: {
      description: {
        story: "Range input with custom validation ensuring the end value is greater than or equal to the start value.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    prefix: "version",
    label: "Version range",
    defaultStart: "1.0",
    defaultEnd: "2.0",
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled range input that cannot be edited.",
      },
    },
  },
};
