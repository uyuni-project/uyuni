import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { RecurringEventPicker } from "./recurring-event-picker";

type RecurringEventPickerProps = React.ComponentProps<typeof RecurringEventPicker>;

const meta = {
  title: "Components/DateTime/RecurringEventPicker",
  component: RecurringEventPicker,
  parameters: {
    docs: {
      description: {
        component:
          "Builds hourly, daily, weekly, monthly, or custom Quartz schedules. Change `type` and `mode` in the controls to review every layout without duplicating stories.",
      },
    },
  },
  args: {
    mode: "Panel",
    hideScheduleName: false,
    scheduleName: "Weekly maintenance",
    type: "weekly",
    cron: "0 15 2 ? * 7",
    cronTimes: {
      minute: 30,
      hour: 2,
      dayOfMonth: "",
      dayOfWeek: "2",
    },
  },
  argTypes: {
    mode: {
      control: "inline-radio",
      options: ["Panel", "Inline"],
      description: "Renders the picker in a bordered panel or directly in the surrounding form.",
    },
    hideScheduleName: {
      control: "boolean",
      description: "Hides the schedule name field when the parent form already provides it.",
    },
    scheduleName: {
      control: "text",
      description: "Initial name shown in the schedule name field.",
    },
    type: {
      control: "select",
      options: ["hourly", "daily", "weekly", "monthly", "cron"],
      description: "Initially selected recurrence type.",
    },
    cron: {
      control: "text",
      description: "Initial custom Quartz expression.",
    },
    cronTimes: {
      control: "object",
      description: "Initial legacy server-time values used by the predefined recurrence types.",
    },
    onScheduleNameChanged: {
      action: "schedule name changed",
      description: "Called when the schedule name changes.",
    },
    onTypeChanged: {
      action: "recurrence type changed",
      description: "Called when a recurrence type is selected.",
    },
    onCronTimesChanged: {
      action: "schedule time changed",
      description: "Called when a predefined schedule value changes.",
    },
    onCronChanged: {
      action: "Quartz expression changed",
      description: "Called when the custom Quartz expression changes.",
    },
  },
  render: (args) => (
    <div style={{ maxWidth: "1100px", minHeight: "520px" }}>
      <RecurringEventPicker
        key={`${args.mode}-${args.hideScheduleName}-${args.scheduleName}-${args.type}-${args.cron}-${JSON.stringify(
          args.cronTimes
        )}`}
        {...args}
      />
    </div>
  ),
} satisfies Meta<RecurringEventPickerProps>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
