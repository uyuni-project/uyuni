import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Text } from "./Text";

type TextProps = React.ComponentProps<typeof Text>;

const StatefulTextInForm = (args: TextProps) => {
  const [model, setModel] = useState({ [args.name]: args.defaultValue || "" });

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
      <Text {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const meta = {
  title: "Components/Inputs/Text",
  component: Text,
  parameters: {
    docs: {
      description: {
        component:
          "Text input field that integrates with the Uyuni form system. Supports validation, labels, hints, and various input types including text, email, url, number, and file.",
      },
    },
  },
  args: {
    name: "username",
    label: "Username",
    defaultValue: "",
    type: "text",
    placeholder: "Enter username",
    required: false,
    disabled: false,
    labelClass: "col-md-3",
    divClass: "col-md-6",
  },
  argTypes: {
    name: {
      control: "text",
      description: "Name of the field to map in the form model. Used as the HTML `name` and `id` attributes.",
      table: { type: { summary: "string" } },
    },
    label: {
      control: "text",
      description: "Label text displayed for the field.",
      table: { type: { summary: "string" } },
    },
    defaultValue: {
      control: "text",
      description: "Initial value if none is set in the form model.",
      table: { type: { summary: "string" } },
    },
    type: {
      control: "select",
      options: ["text", "email", "url", "number", "file"],
      description: "HTML input type attribute.",
      table: { type: { summary: "string" }, defaultValue: { summary: "text" } },
    },
    placeholder: {
      control: "text",
      description: "Placeholder text displayed when the field is empty.",
      table: { type: { summary: "string" } },
    },
    maxLength: {
      control: "number",
      description: "Maximum number of characters allowed.",
      table: { type: { summary: "number" } },
    },
    required: {
      control: "boolean",
      description: "Marks the field as required. Shows validation errors when empty.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables the input field.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    hint: {
      control: "text",
      description: "Help text displayed below the input.",
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
        "Array of validation functions. Each validator receives the value and returns `true` for valid or `false` for invalid. Can be sync or async.",
      table: { type: { summary: "((value: string) => boolean | Promise<boolean>)[]" } },
    },
    labelClass: {
      control: "text",
      description: "CSS classes applied to the label element.",
      table: { type: { summary: "string" } },
    },
    divClass: {
      control: "text",
      description: "CSS classes applied to the wrapper div containing the input and hints.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "CSS classes applied to the form group wrapper.",
      table: { type: { summary: "string" } },
    },
    inputClass: {
      control: "text",
      description: "CSS classes applied directly to the input element.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "HTML title attribute for the input element.",
      table: { type: { summary: "string" } },
    },
    autoComplete: {
      control: "text",
      description: "HTML autocomplete attribute for browser autofill behavior.",
      table: { type: { summary: "string" } },
    },
    onChange: {
      action: "changed",
      description: "Callback invoked when the input value changes. Receives the field name and new value.",
      table: { type: { summary: "(name: string, value: string) => void" } },
    },
  },
  render: (args) => <StatefulTextInForm {...args} />,
} satisfies Meta<typeof Text>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive text input with form integration. Try changing the value and submitting the form.",
      },
    },
  },
};

export const Required: Story = {
  args: {
    name: "email",
    label: "Email address",
    type: "email",
    placeholder: "user@example.com",
    required: true,
    invalidHint: "Please enter a valid email address",
  },
  parameters: {
    docs: {
      description: {
        story: "Required field with validation. Try submitting without entering a value to see the error.",
      },
    },
  },
};

export const WithValidation: Story = {
  args: {
    name: "password",
    label: "Password",
    type: "text",
    required: true,
    invalidHint: "Password must be at least 8 characters",
    validators: [(value: string) => value.length >= 8],
  },
  parameters: {
    docs: {
      description: {
        story: "Text input with custom validation. Password must be at least 8 characters long.",
      },
    },
  },
};

export const WithHint: Story = {
  args: {
    name: "hostname",
    label: "Hostname",
    placeholder: "server.example.com",
    hint: "Fully qualified domain name of the server",
  },
  parameters: {
    docs: {
      description: {
        story: "Text input with a helpful hint displayed below the field.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    name: "readonly-field",
    label: "System ID",
    defaultValue: "1000000001",
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled text input that cannot be edited.",
      },
    },
  },
};

export const NumberInput: Story = {
  args: {
    name: "port",
    label: "Port number",
    type: "number",
    defaultValue: "8080",
    hint: "Port number between 1 and 65535",
  },
  parameters: {
    docs: {
      description: {
        story: "Numeric input field using the HTML5 number type.",
      },
    },
  },
};

export const WithMaxLength: Story = {
  args: {
    name: "username",
    label: "Username",
    maxLength: 32,
    hint: "Maximum 32 characters",
  },
  parameters: {
    docs: {
      description: {
        story: "Text input with a maximum character limit.",
      },
    },
  },
};
