import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";
import { DEPRECATED_Check, DEPRECATED_Select, Password, Radio, Text, TextArea } from "components/input";

import { Form } from "./Form";

type FormProps = React.ComponentProps<typeof Form>;

const meta = {
  title: "Components/Form/UyuniForm",
  component: Form,
  parameters: {
    docs: {
      description: {
        component:
          "Uyuni Form component that provides form context for legacy form inputs. Manages form state, validation, and submission. Use Formik Form for new development - this is provided for backward compatibility.",
      },
    },
  },
  argTypes: {
    model: {
      control: false,
      description: "Form data model object. Each key represents a field name.",
      table: { type: { summary: "Record<string, any>" } },
    },
    onChange: {
      action: "form changed",
      description: "Callback when any form field changes. Receives the updated model.",
      table: { type: { summary: "(model: Record<string, any>) => void" } },
    },
    onSubmit: {
      action: "form submitted",
      description: "Callback when form is submitted.",
      table: { type: { summary: "() => void" } },
    },
    onValidate: {
      control: false,
      description: "Callback for custom form validation.",
      table: { type: { summary: "(isValid: boolean) => void" } },
    },
    divClass: {
      control: "text",
      description: "CSS classes for the form wrapper div.",
      table: { type: { summary: "string" } },
    },
    formDirection: {
      control: "select",
      options: ["form-horizontal", "form-vertical"],
      description: "Form layout direction.",
      table: { type: { summary: "string" }, defaultValue: { summary: "form-horizontal" } },
    },
    className: {
      control: "text",
      description: "CSS classes for the form element.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<FormProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const SimpleFormComponent = () => {
  const [model, setModel] = useState({
    name: "John Doe",
  });

  return (
    <div style={{ padding: "20px" }}>
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
        <Text
          name="name"
          label="Name"
          required
          invalidHint="Minimum 2 characters"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => value.length > 2]}
        />
        <div className="form-group">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <SubmitButton className="btn-primary" text="Submit" />
          </div>
        </div>
      </Form>

      <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Form model:</strong> <code>{JSON.stringify(model, null, 2)}</code>
      </div>
    </div>
  );
};

export const Playground: Story = {
  render: () => <SimpleFormComponent />,
  parameters: {
    docs: {
      description: {
        story:
          "Simple Uyuni form with text input and validation. Try submitting with less than 2 characters to see validation.",
      },
    },
  },
};

const ValidationFormComponent = () => {
  const [model, setModel] = useState({
    username: "",
    email: "",
    age: "",
  });

  return (
    <div style={{ padding: "20px" }}>
      <Form
        model={model}
        onChange={(newModel) => {
          setModel({ ...newModel });
          action("form changed")(newModel);
        }}
        onSubmit={() => action("form submitted")(model)}
        formDirection="form-horizontal"
      >
        <Text
          name="username"
          label="Username"
          required
          invalidHint="Username must be at least 3 characters"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => value.length >= 3]}
        />
        <Text
          name="email"
          label="Email"
          required
          invalidHint="Please enter a valid email address"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)]}
        />
        <Text
          name="age"
          label="Age"
          type="number"
          required
          invalidHint="Age must be between 18 and 120"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => Number(value) >= 18 && Number(value) <= 120]}
        />
        <div className="form-group">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <SubmitButton className="btn-primary" text="Submit" />
          </div>
        </div>
      </Form>

      <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Form model:</strong> <code>{JSON.stringify(model, null, 2)}</code>
      </div>
    </div>
  );
};

export const Validation: Story = {
  render: () => <ValidationFormComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Form with multiple fields and custom validation rules. Each field has specific validation requirements.",
      },
    },
  },
};

const MultipleFieldsComponent = () => {
  const [model, setModel] = useState({
    serverName: "web-server-01",
    description: "",
    enabled: true,
    role: "application",
    environment: "production",
  });

  return (
    <div style={{ padding: "20px" }}>
      <Form
        model={model}
        onChange={(newModel) => {
          setModel({ ...newModel });
          action("form changed")(newModel);
        }}
        onSubmit={() => action("form submitted")(model)}
        formDirection="form-horizontal"
      >
        <Text
          name="serverName"
          label="Server Name"
          required
          invalidHint="Server name is required"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => value.length > 0]}
        />

        <TextArea name="description" label="Description" labelClass="col-md-3" divClass="col-md-6" rows={4} />

        <DEPRECATED_Check name="enabled" label="Enabled" labelClass="col-md-3" divClass="col-md-6" />

        <Radio
          name="role"
          label="Server Role"
          items={[
            { label: "Application", value: "application" },
            { label: "Database", value: "database" },
            { label: "Web", value: "web" },
          ]}
          labelClass="col-md-3"
          divClass="col-md-6"
        />

        <DEPRECATED_Select name="environment" label="Environment" labelClass="col-md-3" divClass="col-md-6">
          <option value="development">Development</option>
          <option value="staging">Staging</option>
          <option value="production">Production</option>
        </DEPRECATED_Select>

        <div className="form-group">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <SubmitButton className="btn-primary" text="Save Configuration" />
          </div>
        </div>
      </Form>

      <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Form model:</strong> <code>{JSON.stringify(model, null, 2)}</code>
      </div>
    </div>
  );
};

export const MultipleFields: Story = {
  render: () => <MultipleFieldsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Form with multiple field types: text, textarea, checkbox, radio, and select.",
      },
    },
  },
};

const PasswordFormComponent = () => {
  const [model, setModel] = useState({
    username: "",
    password: "",
    confirmPassword: "",
  });

  return (
    <div style={{ padding: "20px" }}>
      <Form
        model={model}
        onChange={(newModel) => {
          setModel({ ...newModel });
          action("form changed")(newModel);
        }}
        onSubmit={() => action("form submitted")(model)}
        formDirection="form-horizontal"
      >
        <Text
          name="username"
          label="Username"
          required
          invalidHint="Username is required"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => value.length > 0]}
        />

        <Password
          name="password"
          label="Password"
          required
          invalidHint="Password must be at least 8 characters"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => value.length >= 8]}
        />

        <Password
          name="confirmPassword"
          label="Confirm Password"
          required
          invalidHint="Passwords must match"
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value) => value === model.password]}
        />

        <div className="form-group">
          <div className="col-md-offset-3 offset-md-3 col-md-6">
            <SubmitButton className="btn-primary" text="Create Account" />
          </div>
        </div>
      </Form>

      <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Form model:</strong>{" "}
        <code>{JSON.stringify({ ...model, password: "***", confirmPassword: "***" }, null, 2)}</code>
      </div>
    </div>
  );
};

export const PasswordForm: Story = {
  render: () => <PasswordFormComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Form with password fields and confirmation validation. Password must be at least 8 characters and both passwords must match.",
      },
    },
  },
};
