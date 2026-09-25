import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { localizedMoment } from "utils";

import { DateTimePicker } from "./DateTimePicker";

const StatefulDateTimePicker = (props: Omit<React.ComponentProps<typeof DateTimePicker>, "value" | "onChange">) => {
  const [value, setValue] = useState(localizedMoment());

  return (
    <div>
      <DateTimePicker
        {...props}
        value={value}
        onChange={(newValue) => {
          setValue(newValue);
          action("changed")(newValue.toISOString());
        }}
      />
      <div style={{ marginTop: "1rem", padding: "10px", backgroundColor: "#f5f5f5", borderRadius: "4px" }}>
        <p>
          <strong>Selected date/time:</strong>
        </p>
        <p>ISO: {value.toISOString()}</p>
        <p>User timezone: {localizedMoment(value).tz(localizedMoment.userTimeZone).format("YYYY-MM-DD HH:mm:ss Z")}</p>
      </div>
    </div>
  );
};

const meta = {
  title: "Components/DateTime/DateTimePicker",
  component: DateTimePicker,
  parameters: {
    docs: {
      description: {
        component:
          "Date and time picker component powered by react-datepicker. Handles timezone conversions between user timezone, server timezone, and UTC. Supports separate date and time pickers or combined selection.",
      },
    },
  },
  args: {
    id: "datetime-picker",
    disabled: false,
    hideDatePicker: false,
    hideTimePicker: false,
    serverTimeZone: false,
  },
  argTypes: {
    value: {
      control: false,
      description: "Current date/time value as a moment.js object.",
      table: { type: { summary: "moment.Moment" } },
    },
    onChange: {
      action: "changed",
      description: "Callback invoked when date/time changes. Receives a moment.Moment object.",
      table: { type: { summary: "(value: moment.Moment) => void" } },
    },
    id: {
      control: "text",
      description: "HTML identifier for the picker inputs (suffixed with _date and _time).",
      table: { type: { summary: "string" } },
    },
    legacyId: {
      control: "text",
      description: "Legacy ID format for compatibility with Java DateTimePickerTag.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Disable the date and time pickers.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    hideDatePicker: {
      control: "boolean",
      description: "Hide the date picker, showing only the time picker.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    hideTimePicker: {
      control: "boolean",
      description: "Hide the time picker, showing only the date picker.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    serverTimeZone: {
      control: "boolean",
      description: "Use server timezone instead of user's configured timezone.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
  },
} satisfies Meta<typeof DateTimePicker>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: (args) => <StatefulDateTimePicker {...args} />,
  parameters: {
    docs: {
      description: {
        story:
          "Interactive date/time picker. Click the calendar icon to select a date, click the clock icon to select a time.",
      },
    },
  },
};

export const DateOnly: Story = {
  args: {
    hideTimePicker: true,
  },
  render: (args) => <StatefulDateTimePicker {...args} />,
  parameters: {
    docs: {
      description: {
        story: "Date picker only, without time selection.",
      },
    },
  },
};

export const TimeOnly: Story = {
  args: {
    hideDatePicker: true,
  },
  render: (args) => <StatefulDateTimePicker {...args} />,
  parameters: {
    docs: {
      description: {
        story: "Time picker only, without date selection.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
  render: (args) => <StatefulDateTimePicker {...args} />,
  parameters: {
    docs: {
      description: {
        story: "Disabled date/time picker that cannot be interacted with.",
      },
    },
  },
};

export const ServerTimeZone: Story = {
  args: {
    serverTimeZone: true,
  },
  render: (args) => <StatefulDateTimePicker {...args} />,
  parameters: {
    docs: {
      description: {
        story:
          "Date/time picker using server timezone instead of user timezone. Check the displayed values to see the timezone difference.",
      },
    },
  },
};
