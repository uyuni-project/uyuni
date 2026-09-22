import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";
import { Field, Form, MultiField, OnSubmit } from "components/formik";

import { localizedMoment } from "utils/datetime";
import { Utils } from "utils/functions";

type FormProps = React.ComponentProps<typeof Form>;

const meta = {
  title: "Components/Form/FormikForm",
  component: Form,
  parameters: {
    docs: {
      description: {
        component:
          "Formik-based form component for Uyuni. Provides form state management, validation, and a comprehensive set of field components. This is the recommended form system for new development.",
      },
    },
  },
  argTypes: {
    initialValues: {
      control: false,
      description: "Initial form values object. Each key represents a field name.",
      table: { type: { summary: "Record<string, any>" } },
    },
    onSubmit: {
      control: false,
      description: "Callback when form is submitted. Receives values and Formik helpers.",
      table: { type: { summary: "(values: any, helpers: FormikHelpers) => void | Promise<void>" } },
    },
    validationSchema: {
      control: false,
      description: "Yup validation schema for form validation.",
      table: { type: { summary: "Yup.ObjectSchema" } },
    },
    labelClass: {
      control: "text",
      description: "CSS classes for field labels.",
      table: { type: { summary: "string" } },
    },
    divClass: {
      control: "text",
      description: "CSS classes for field wrapper divs.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<FormProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const SimpleFormComponent = () => {
  const initialValues = {
    username: "",
    email: "",
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values) => {
    action("form submitted")(values);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
        {({ isSubmitting, values }) => (
          <>
            <Field
              name="username"
              label="Username"
              required
              validate={(value) => (value.length < 3 ? "Minimum 3 characters" : "")}
            />

            <Field
              name="email"
              label="Email"
              type="email"
              required
              validate={(value) => (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? "" : "Invalid email address")}
            />

            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">
                <SubmitButton className="btn-primary" text="Submit" disabled={isSubmitting} />
              </div>
            </div>

            <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
              <strong>Form values:</strong> <code>{JSON.stringify(values, null, 2)}</code>
            </div>
          </>
        )}
      </Form>
    </div>
  );
};

export const Playground: Story = {
  render: () => <SimpleFormComponent />,
  parameters: {
    docs: {
      description: {
        story: "Simple Formik form with text and email fields. Try submitting invalid data to see validation errors.",
      },
    },
  },
};

const AllFieldTypesComponent = () => {
  const initialValues = {
    field: "field value",
    checkbox: false,
    password: Utils.generatePassword(),
    datetime: localizedMoment(),
    multiple: [] as string[],
    longText: "multiple\nlines",
    range_start: "1000",
    range_end: "1100",
    radio: "one",
    select: "foo",
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values) => {
    action("form submitted")(values);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
        {({ isSubmitting, values }) => (
          <>
            <Field
              name="field"
              label="Text Field"
              required
              validate={(value) => (value.length < 2 ? "Minimum 2 characters" : "")}
            />

            <Field name="checkbox" label="Checkbox" as={Field.Check} />

            <Field name="password" label="Password" as={Field.Password} />

            <Field name="datetime" label="Date & Time" as={Field.DateTimePicker} />

            <MultiField name="multiple" label="Multiple Values" defaultNewItemValue="" />

            <Field name="longText" label="Long Text" as={Field.TextArea} rows={10} />

            <Field name="range" label="Range" as={Field.Range} />

            <Field
              name="radio"
              label="Radio"
              items={[
                { label: "One", value: "one" },
                { label: "Two", value: "two" },
                { label: "Three", value: "three" },
              ]}
              as={Field.Radio}
            />

            <Field
              name="select"
              label="Select"
              options={[
                { label: "Foo", value: "foo" },
                { label: "Bar", value: "bar" },
              ]}
              as={Field.Select}
            />

            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">
                <SubmitButton className="btn-primary" text="Submit" disabled={isSubmitting} />
              </div>
            </div>

            <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
              <strong>Form values:</strong>
              <pre style={{ marginTop: "10px", fontSize: "12px" }}>{JSON.stringify(values, null, 2)}</pre>
            </div>
          </>
        )}
      </Form>
    </div>
  );
};

export const AllFieldTypes: Story = {
  render: () => <AllFieldTypesComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Comprehensive form demonstrating all available Formik field types: text, checkbox, password, datetime, multiple values, textarea, range, radio, and select.",
      },
    },
  },
};

const ValidationComponent = () => {
  const initialValues = {
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    age: "",
    terms: false,
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values) => {
    action("form submitted")(values);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
        {({ isSubmitting, values }) => (
          <>
            <Field
              name="username"
              label="Username"
              required
              validate={(value) => {
                if (value.length < 3) return "Username must be at least 3 characters";
                if (!/^[a-zA-Z0-9_]+$/.test(value))
                  return "Username can only contain letters, numbers, and underscores";
                return "";
              }}
            />

            <Field
              name="email"
              label="Email"
              type="email"
              required
              validate={(value) => (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? "" : "Invalid email address")}
            />

            <Field
              name="password"
              label="Password"
              as={Field.Password}
              required
              validate={(value) => {
                if (value.length < 8) return "Password must be at least 8 characters";
                if (!/[A-Z]/.test(value)) return "Password must contain at least one uppercase letter";
                if (!/[0-9]/.test(value)) return "Password must contain at least one number";
                return "";
              }}
            />

            <Field
              name="confirmPassword"
              label="Confirm Password"
              as={Field.Password}
              required
              validate={(value) => (value === values.password ? "" : "Passwords must match")}
            />

            <Field
              name="age"
              label="Age"
              type="number"
              required
              validate={(value) => {
                const age = Number(value);
                if (isNaN(age)) return "Age must be a number";
                if (age < 18) return "You must be at least 18 years old";
                if (age > 120) return "Invalid age";
                return "";
              }}
            />

            <Field
              name="terms"
              label="I accept the terms and conditions"
              as={Field.Check}
              validate={(value) => (value ? "" : "You must accept the terms to continue")}
            />

            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">
                <SubmitButton className="btn-primary" text="Create Account" disabled={isSubmitting} />
              </div>
            </div>

            <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
              <strong>Form values:</strong>
              <pre style={{ marginTop: "10px", fontSize: "12px" }}>
                {JSON.stringify({ ...values, password: "***", confirmPassword: "***" }, null, 2)}
              </pre>
            </div>
          </>
        )}
      </Form>
    </div>
  );
};

export const Validation: Story = {
  render: () => <ValidationComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Form with comprehensive validation rules. Each field has specific validation requirements that are checked in real-time and on submission.",
      },
    },
  },
};

const ServerConfigComponent = () => {
  const initialValues = {
    name: "",
    description: "",
    enabled: true,
    ports: ["80", "443"],
    environment: "production",
    logLevel: "info",
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values) => {
    action("form submitted")(values);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
        {({ isSubmitting, values }) => (
          <>
            <Field name="name" label="Server Name" required validate={(value) => (value ? "" : "Name is required")} />

            <Field name="description" label="Description" as={Field.TextArea} rows={4} />

            <Field name="enabled" label="Enabled" as={Field.Check} />

            <MultiField
              name="ports"
              label="Ports"
              defaultNewItemValue=""
              validate={(value) => (value && value.length > 0 ? "" : "At least one port is required")}
            />

            <Field
              name="environment"
              label="Environment"
              options={[
                { label: "Development", value: "development" },
                { label: "Staging", value: "staging" },
                { label: "Production", value: "production" },
              ]}
              as={Field.Select}
            />

            <Field
              name="logLevel"
              label="Log Level"
              items={[
                { label: "Debug", value: "debug" },
                { label: "Info", value: "info" },
                { label: "Warning", value: "warning" },
                { label: "Error", value: "error" },
              ]}
              as={Field.Radio}
            />

            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">
                <SubmitButton className="btn-primary" text="Save Configuration" disabled={isSubmitting} />
              </div>
            </div>

            <div style={{ marginTop: "20px", padding: "15px", background: "#f5f5f5", borderRadius: "4px" }}>
              <strong>Form values:</strong>
              <pre style={{ marginTop: "10px", fontSize: "12px" }}>{JSON.stringify(values, null, 2)}</pre>
            </div>
          </>
        )}
      </Form>
    </div>
  );
};

export const ServerConfiguration: Story = {
  render: () => <ServerConfigComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Practical example of a server configuration form using multiple field types including MultiField for managing port arrays.",
      },
    },
  },
};
