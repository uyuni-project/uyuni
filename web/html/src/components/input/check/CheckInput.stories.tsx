import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button, SubmitButton } from "components/buttons";
import { Field, Form, OnSubmit } from "components/formik";

import { Check } from "./Check";
import { CheckInput } from "./CheckInput";

type CheckInputProps = React.ComponentProps<typeof CheckInput>;

const ControlledCheckInput = (props: CheckInputProps) => {
  const [checked, setChecked] = useState(props.checked ?? false);

  useEffect(() => setChecked(props.checked ?? false), [props.checked]);

  return (
    <CheckInput
      {...props}
      checked={checked}
      onChange={(nextChecked) => {
        setChecked(nextChecked);
        props.onChange?.(nextChecked);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/CheckInput",
  component: CheckInput,
  parameters: {
    docs: {
      description: {
        component:
          "Low-level checkbox input with support for the DOM-only indeterminate state. Use `Check` when the standard Uyuni label wrapper is also needed.",
      },
    },
  },
  args: {
    "aria-label": "Select item",
    checked: false,
    disabled: false,
    indeterminate: false,
  },
  argTypes: {
    indeterminate: {
      control: "boolean",
      description: "Sets the checkbox's DOM `indeterminate` property.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    checked: {
      control: "boolean",
      description: "Current checked state of the checkbox.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents the checkbox from being changed.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    onChange: {
      action: "changed",
      description: "Called with the checkbox's next checked value.",
      table: { type: { summary: "(checked: boolean) => void" } },
    },
  },
  render: (args) => <ControlledCheckInput {...args} />,
} satisfies Meta<typeof CheckInput>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <CheckInput aria-label="Unchecked" />
        <CheckInput aria-label="Checked" defaultChecked />
        <CheckInput aria-label="Indeterminate" indeterminate />
        <CheckInput aria-label="Disabled" disabled />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Unchecked, checked, indeterminate, and disabled native states." } },
  },
};

const StandaloneComponent = () => {
  const [checked, setChecked] = useState(true);

  return (
    <div style={{ padding: "20px" }}>
      <label style={{ display: "flex", alignItems: "center", gap: "8px", cursor: "pointer" }}>
        <Check
          name="standalone-check"
          checked={checked}
          onChange={(newChecked) => {
            setChecked(newChecked);
            action("changed")(newChecked);
          }}
        />
        <span>Accept terms and conditions</span>
      </label>

      <div style={{ marginTop: "20px", padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>State:</strong> <code>checked={String(checked)}</code>
      </div>
    </div>
  );
};

export const Standalone: Story = {
  render: () => <StandaloneComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Standalone controlled checkbox without form integration. Click to toggle the checked state.",
      },
    },
  },
};

const IndeterminateComponent = () => {
  const [checked, setChecked] = useState(false);
  const [indeterminate, setIndeterminate] = useState(true);

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ display: "grid", gap: "12px", justifyItems: "start" }}>
        <label style={{ display: "flex", alignItems: "center", gap: "8px", cursor: "pointer" }}>
          <Check
            checked={checked}
            indeterminate={indeterminate}
            onChange={(newChecked) => {
              setChecked(newChecked);
              action("changed")(newChecked);
            }}
          />
          <span>Click to toggle</span>
        </label>

        <div className="btn-group">
          <Button
            className="btn-default"
            text={indeterminate ? "Clear indeterminate" : "Set indeterminate"}
            handler={() => setIndeterminate((prev) => !prev)}
          />
          <Button
            className="btn-default"
            text={checked ? "Uncheck" : "Check"}
            handler={() => setChecked((prev) => !prev)}
          />
        </div>

        <div style={{ padding: "10px", background: "#f5f5f5", borderRadius: "4px", width: "100%" }}>
          <strong>State:</strong> <code>checked={String(checked)}</code>{" "}
          <code>indeterminate={String(indeterminate)}</code>
        </div>
      </div>
    </div>
  );
};

export const Indeterminate: Story = {
  render: () => <IndeterminateComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Checkbox with indeterminate state. The indeterminate state is typically used for 'select all' checkboxes where some but not all items are selected. Use the buttons to control the state independently.",
      },
    },
  },
};

const FormikIntegrationComponent = () => {
  const initialValues = {
    acceptTerms: false,
  };

  const onSubmit: OnSubmit<typeof initialValues> = async (values) => {
    action("form submitted")(values);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Form initialValues={initialValues} onSubmit={onSubmit} labelClass="col-md-3" divClass="col-md-6">
        {({ isSubmitting, values }) => (
          <>
            <Field name="acceptTerms" label="I accept the terms and conditions" as={Field.Check} />

            <div className="form-group">
              <div className="col-md-offset-3 offset-md-3 col-md-6">
                <SubmitButton className="btn-primary" text="Submit" disabled={isSubmitting} />
              </div>
            </div>

            <div style={{ padding: "15px", background: "#f5f5f5", borderRadius: "4px", marginTop: "20px" }}>
              <strong>Form values:</strong> <code>{JSON.stringify(values, null, 2)}</code>
            </div>
          </>
        )}
      </Form>
    </div>
  );
};

export const FormikIntegration: Story = {
  render: () => <FormikIntegrationComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Checkbox integrated with Formik form using Field.Check. The checkbox value is managed by Formik and included in form submission.",
      },
    },
  },
};
