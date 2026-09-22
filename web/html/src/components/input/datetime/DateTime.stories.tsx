import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SubmitButton } from "components/buttons";

import { localizedMoment } from "utils";

import { Form } from "../form/Form";
import { DateTime } from "./DateTime";

type DateTimeProps = React.ComponentProps<typeof DateTime>;

const StatefulDateTimeInForm = (args: DateTimeProps) => {
  const [model, setModel] = useState({ [args.name]: args.defaultValue || localizedMoment() });

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
      <DateTime {...args} />
      <div className="form-group">
        <div className="col-md-offset-3 offset-md-3 col-md-6">
          <SubmitButton className="btn-primary" text="Submit" disabled={args.disabled} />
        </div>
      </div>
    </Form>
  );
};

const meta = {
  title: "Components/Inputs/DateTime",
  component: DateTime,
  parameters: {
    docs: {
      description: {
        component:
          "Date/time input field that integrates the DateTimePicker component with the Uyuni form system. Handles moment.js values and timezone conversions automatically.",
      },
    },
  },
  args: {
    name: "scheduled_date",
    label: "Scheduled date",
    defaultValue: localizedMoment(),
    required: false,
    disabled: false,
    labelClass: "col-md-3",
    divClass: "col-md-6",
  },
  argTypes: {
    name: {
      control: "text",
      description: "Name of the field to map in the form model. Used as the HTML `name` attribute.",
      table: { type: { summary: "string" } },
    },
    label: {
      control: "text",
      description: "Label text displayed for the date/time input.",
      table: { type: { summary: "string" } },
    },
    defaultValue: {
      control: false,
      description: "Initial value if none is set in the form model. Must be a moment.Moment object.",
      table: { type: { summary: "moment.Moment" } },
    },
    required: {
      control: "boolean",
      description: "Marks the field as required. Shows validation errors when empty.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables the date/time picker.",
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
        "Array of validation functions. Each validator receives the moment.Moment value and returns `true` for valid or `false` for invalid.",
      table: { type: { summary: "((value: moment.Moment) => boolean | Promise<boolean>)[]" } },
    },
    labelClass: {
      control: "text",
      description: "CSS classes applied to the label element.",
      table: { type: { summary: "string" } },
    },
    divClass: {
      control: "text",
      description: "CSS classes applied to the wrapper div containing the picker and hints.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "CSS classes applied to the form group wrapper.",
      table: { type: { summary: "string" } },
    },
  },
  render: (args) => <StatefulDateTimeInForm {...args} />,
} satisfies Meta<typeof DateTime>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Interactive date/time input integrated with the form system. Click the calendar and clock icons to select date and time.",
      },
    },
  },
};

export const ScheduledAction: Story = {
  args: {
    name: "execution_time",
    label: "Execution time",
    defaultValue: localizedMoment().add(1, "hour"),
    hint: "Select when this action should be executed",
  },
  parameters: {
    docs: {
      description: {
        story: "Date/time input for scheduling an action, defaulting to 1 hour from now.",
      },
    },
  },
};

export const Required: Story = {
  args: {
    name: "due_date",
    label: "Due date",
    required: true,
    invalidHint: "Due date is required",
    hint: "Select the deadline for this task",
  },
  parameters: {
    docs: {
      description: {
        story: "Required date/time field. Try submitting without selecting a date to see validation.",
      },
    },
  },
};

export const WithValidation: Story = {
  args: {
    name: "future_date",
    label: "Future date",
    defaultValue: localizedMoment(),
    required: true,
    invalidHint: "Date must be in the future",
    validators: [(value: moment.Moment) => value.isAfter(localizedMoment())],
    hint: "Select a date in the future",
  },
  parameters: {
    docs: {
      description: {
        story: "Date/time input with custom validation ensuring the selected date is in the future.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    name: "locked_date",
    label: "Locked date",
    defaultValue: localizedMoment(),
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled date/time input that cannot be changed.",
      },
    },
  },
};
