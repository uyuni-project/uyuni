import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Password } from "./Password";

type PasswordProps = React.ComponentProps<typeof Password>;

const StatefulPasswordInForm = (args: PasswordProps) => {
  const [model, setModel] = useState({ [args.name]: args.defaultValue || "" });

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
      <Password {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const meta = {
  title: "Components/Inputs/Password",
  component: Password,
  parameters: {
    docs: {
      description: {
        component:
          "Password input field that masks the entered text. Built on top of the Text component with `type='password'`. Integrates with the Uyuni form system and supports validation.",
      },
    },
  },
  args: {
    name: "password",
    label: "Password",
    defaultValue: "",
    placeholder: "Enter password",
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
    placeholder: {
      control: "text",
      description: "Placeholder text displayed when the field is empty.",
      table: { type: { summary: "string" } },
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
      description: "HTML autocomplete attribute. Use 'current-password' or 'new-password' for password managers.",
      table: { type: { summary: "string" } },
    },
    onChange: {
      action: "changed",
      description: "Callback invoked when the input value changes. Receives the field name and new value.",
      table: { type: { summary: "(name: string, value: string) => void" } },
    },
  },
  render: (args) => <StatefulPasswordInForm {...args} />,
} satisfies Meta<typeof Password>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive password input with form integration. The entered text is masked for security.",
      },
    },
  },
};

export const Required: Story = {
  args: {
    name: "password",
    label: "Current password",
    required: true,
    invalidHint: "Password is required",
    autoComplete: "current-password",
  },
  parameters: {
    docs: {
      description: {
        story: "Required password field. Try submitting without entering a value to see the validation error.",
      },
    },
  },
};

export const WithValidation: Story = {
  args: {
    name: "new-password",
    label: "New password",
    required: true,
    invalidHint: "Password must be at least 12 characters and contain letters and numbers",
    hint: "Use a strong password with at least 12 characters",
    autoComplete: "new-password",
    validators: [
      (value: string) => value.length >= 12,
      (value: string) => /[a-zA-Z]/.test(value),
      (value: string) => /[0-9]/.test(value),
    ],
  },
  parameters: {
    docs: {
      description: {
        story:
          "Password input with multiple validation rules: minimum 12 characters, must contain letters and numbers.",
      },
    },
  },
};

const PasswordConfirmationComponent = () => {
  const [model, setModel] = useState({ password: "", confirm: "" });

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
      <Password
        name="password"
        label="New password"
        required
        invalidHint="Password must be at least 8 characters"
        validators={[(value: string) => value.length >= 8]}
        labelClass="col-md-3"
        divClass="col-md-6"
        autoComplete="new-password"
      />
      <Password
        name="confirm"
        label="Confirm password"
        required
        invalidHint="Passwords must match"
        validators={[(value: string) => value === model.password]}
        labelClass="col-md-3"
        divClass="col-md-6"
        autoComplete="new-password"
      />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Change password" disabled={model.password !== model.confirm} />
        </div>
      </div>
    </Form>
  );
};

export const PasswordConfirmation: Story = {
  render: () => <PasswordConfirmationComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Common password change pattern with confirmation. The second field validates that both passwords match.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    name: "password",
    label: "Password",
    defaultValue: "********",
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled password input that cannot be edited.",
      },
    },
  },
};
