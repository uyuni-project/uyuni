import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { DEPRECATED_Check } from "./DEPRECATED_Check";

type DEPRECATED_CheckProps = React.ComponentProps<typeof DEPRECATED_Check>;

const StatefulCheckInForm = (args: DEPRECATED_CheckProps) => {
  const [model, setModel] = useState({ [args.name]: args.defaultValue || false });

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        setModel({ ...newModel });
        action("form changed")(newModel);
      }}
      onSubmit={() => action("form submitted")(model)}
      divClass="col-md-12"
    >
      <DEPRECATED_Check {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const meta = {
  title: "Deprecated/Inputs/DEPRECATED_Check",
  component: DEPRECATED_Check,
  parameters: {
    docs: {
      description: {
        component: `
**⚠️ DEPRECATED - Do not use in new code**

This component is deprecated. Use one of the following instead:
- For standalone checkboxes: \`import { Check } from "components/input"\`
- For Formik forms: \`<Field as={Field.Check} />\`

This component uses the old form context and will be removed in a future release.
        `,
      },
    },
  },
  args: {
    name: "accept",
    label: "I accept the terms and conditions",
    defaultValue: false,
    required: false,
    disabled: false,
  },
  argTypes: {
    name: {
      control: "text",
      description: "Name of the field in the form model.",
      table: { type: { summary: "string" } },
    },
    label: {
      control: "text",
      description: "Label text displayed next to the checkbox.",
      table: { type: { summary: "ReactNode" } },
    },
    defaultValue: {
      control: "boolean",
      description: "Initial checked state.",
      table: { type: { summary: "boolean" } },
    },
    required: {
      control: "boolean",
      description: "Makes the checkbox required.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables the checkbox.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    title: {
      control: "text",
      description: "HTML title attribute for tooltip.",
      table: { type: { summary: "string" } },
    },
    inputClass: {
      control: "text",
      description: "CSS class for the input element.",
      table: { type: { summary: "string" } },
    },
  },
  render: (args) => <StatefulCheckInForm {...args} />,
} satisfies Meta<DEPRECATED_CheckProps>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "⚠️ DEPRECATED - Interactive checkbox. Check the box to see the form state change. Use CheckInput for new code.",
      },
    },
  },
};

export const Required: Story = {
  args: {
    name: "terms",
    label: "I agree to the terms of service",
    required: true,
  },
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Required checkbox. Try submitting without checking to see validation.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    name: "feature",
    label: "Enable advanced features",
    defaultValue: true,
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Disabled checkbox that cannot be changed.",
      },
    },
  },
};

const MultipleCheckboxesComponent = () => {
  const [model, setModel] = useState({
    email: false,
    sms: false,
    push: false,
  });

  return (
    <Form
      model={model}
      onChange={(newModel) => setModel({ ...newModel })}
      onSubmit={() => action("form submitted")(model)}
    >
      <DEPRECATED_Check name="email" label="Email notifications" />
      <DEPRECATED_Check name="sms" label="SMS notifications" />
      <DEPRECATED_Check name="push" label="Push notifications" />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Save Preferences" />
        </div>
      </div>
    </Form>
  );
};

export const MultipleCheckboxes: Story = {
  render: () => <MultipleCheckboxesComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Multiple checkboxes in a form.",
      },
    },
  },
};
