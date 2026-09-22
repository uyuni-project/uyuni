import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Radio } from "./Radio";

type RadioProps = React.ComponentProps<typeof Radio>;

const StatefulRadioInForm = (args: RadioProps) => {
  const [model, setModel] = useState({ [args.name || "radio"]: args.defaultValue || "" });

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        setModel({ ...newModel });
        action("form changed")(newModel);
      }}
      onSubmit={() => {
        action("form submitted")(model);
      }}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Radio {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const priorityOptions = [
  { label: "Low", value: "low" },
  { label: "Normal", value: "normal" },
  { label: "High", value: "high" },
  { label: "Critical", value: "critical" },
];

const actionOptions = [
  { label: "Apply updates", value: "update" },
  { label: "Restart system", value: "restart" },
  { label: "Power off", value: "poweroff" },
];

const meta = {
  title: "Components/Inputs/Radio",
  component: Radio,
  parameters: {
    docs: {
      description: {
        component:
          "Radio button group for selecting a single option from multiple choices. Supports vertical and horizontal layouts, disabled options, and an open text input option for custom values.",
      },
    },
  },
  args: {
    name: "priority",
    label: "Priority",
    items: priorityOptions,
    defaultValue: "normal",
    inline: false,
    openOption: false,
    required: false,
    disabled: false,
    labelClass: "col-md-3",
    divClass: "col-md-6",
  },
  argTypes: {
    name: {
      control: "text",
      description: "Name of the field to map in the form model. Used as the HTML `name` attribute for all radios.",
      table: { type: { summary: "string" } },
    },
    label: {
      control: "text",
      description: "Label text displayed for the radio group.",
      table: { type: { summary: "string" } },
    },
    items: {
      control: "object",
      description: "Array of radio options with label, value, optional title, and optional disabled state.",
      table: {
        type: {
          summary: "{ label: ReactNode; value: string; title?: string; disabled?: boolean }[]",
        },
      },
    },
    defaultValue: {
      control: "text",
      description: "Initial selected value if none is set in the form model.",
      table: { type: { summary: "string" } },
    },
    inline: {
      control: "boolean",
      description: "Display radio buttons horizontally in a line instead of vertically stacked.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    openOption: {
      control: "boolean",
      description: 'Add a custom text input option labeled "Other keyword" for values not in the predefined list.',
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    required: {
      control: "boolean",
      description: "Marks the field as required. Shows validation errors when no option is selected.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables all radio buttons in the group.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    hint: {
      control: "text",
      description: "Help text displayed below the radio group.",
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
        "Array of validation functions. Each validator receives the value and returns `true` for valid or `false` for invalid.",
      table: { type: { summary: "((value: string) => boolean | Promise<boolean>)[]" } },
    },
    labelClass: {
      control: "text",
      description: "CSS classes applied to the label element.",
      table: { type: { summary: "string" } },
    },
    divClass: {
      control: "text",
      description: "CSS classes applied to the wrapper div containing the radios and hints.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "CSS classes applied to the form group wrapper.",
      table: { type: { summary: "string" } },
    },
    inputClass: {
      control: "text",
      description: "CSS classes applied to each radio input element.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "HTML title attribute for the radio inputs.",
      table: { type: { summary: "string" } },
    },
    onChange: {
      action: "changed",
      description: "Callback invoked when the selected value changes. Receives the field name and new value.",
      table: { type: { summary: "(name: string, value: string) => void" } },
    },
  },
  render: (args) => <StatefulRadioInForm {...args} />,
} satisfies Meta<typeof Radio>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive radio button group. Select an option to see the form model update.",
      },
    },
  },
};

export const Inline: Story = {
  args: {
    name: "severity",
    label: "Severity",
    items: [
      { label: "Info", value: "info" },
      { label: "Warning", value: "warning" },
      { label: "Error", value: "error" },
    ],
    defaultValue: "info",
    inline: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Radio buttons displayed horizontally in a single line.",
      },
    },
  },
};

export const Required: Story = {
  args: {
    name: "action",
    label: "Action to perform",
    items: actionOptions,
    required: true,
    invalidHint: "Please select an action",
  },
  parameters: {
    docs: {
      description: {
        story: "Required radio group. Try submitting without selecting an option to see the validation error.",
      },
    },
  },
};

export const WithOpenOption: Story = {
  args: {
    name: "keyword",
    label: "Search keyword",
    items: [
      { label: "Package name", value: "package" },
      { label: "CVE number", value: "cve" },
      { label: "Advisory ID", value: "advisory" },
    ],
    defaultValue: "package",
    openOption: true,
    hint: 'Select a predefined keyword or choose "Other keyword" to enter a custom value',
  },
  parameters: {
    docs: {
      description: {
        story: 'Radio group with an open text input option. Select "Other keyword" to enable the custom text field.',
      },
    },
  },
};

export const WithDisabledOptions: Story = {
  args: {
    name: "environment",
    label: "Deployment environment",
    items: [
      { label: "Development", value: "dev" },
      { label: "Staging", value: "staging" },
      { label: "Production", value: "prod", disabled: true, title: "Production deployment requires approval" },
    ],
    defaultValue: "dev",
  },
  parameters: {
    docs: {
      description: {
        story:
          "Radio group with some options disabled. The Production option cannot be selected (hover to see the tooltip).",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    name: "priority",
    label: "Priority",
    items: priorityOptions,
    defaultValue: "high",
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Entire radio group disabled. No options can be selected or changed.",
      },
    },
  },
};

export const WithValidation: Story = {
  args: {
    name: "agreement",
    label: "Terms and conditions",
    items: [
      { label: "I accept the terms and conditions", value: "accept" },
      { label: "I decline", value: "decline" },
    ],
    required: true,
    invalidHint: "You must accept the terms to continue",
    validators: [(value: string) => value === "accept"],
  },
  parameters: {
    docs: {
      description: {
        story: "Radio group with custom validation. Only 'accept' is valid, selecting 'decline' shows an error.",
      },
    },
  },
};
