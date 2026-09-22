import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { localizedMoment } from "utils";

import { FromNow } from "./FromNow";

const timePresets = {
  "30 seconds ago": localizedMoment().subtract(30, "seconds").toISOString(),
  "5 minutes ago": localizedMoment().subtract(5, "minutes").toISOString(),
  "30 minutes ago": localizedMoment().subtract(30, "minutes").toISOString(),
  "1 hour ago": localizedMoment().subtract(1, "hour").toISOString(),
  "2 hours ago": localizedMoment().subtract(2, "hours").toISOString(),
  "5 hours ago": localizedMoment().subtract(5, "hours").toISOString(),
  "1 day ago": localizedMoment().subtract(1, "day").toISOString(),
  "3 days ago": localizedMoment().subtract(3, "days").toISOString(),
  "1 week ago": localizedMoment().subtract(1, "week").toISOString(),
  "1 month ago": localizedMoment().subtract(1, "month").toISOString(),
  "6 months ago": localizedMoment().subtract(6, "months").toISOString(),
  "1 year ago": localizedMoment().subtract(1, "year").toISOString(),
  "in 1 hour": localizedMoment().add(1, "hour").toISOString(),
  "in 1 day": localizedMoment().add(1, "day").toISOString(),
  "in 1 week": localizedMoment().add(1, "week").toISOString(),
};

const meta = {
  title: "Components/DateTime/FromNow",
  component: FromNow,
  parameters: {
    docs: {
      description: {
        component:
          "Displays relative time (e.g., '2 hours ago', '3 days ago') from a given date/time. Uses moment.js `fromNow()` and renders as a semantic HTML `<time>` element with proper title and dateTime attributes for accessibility.",
      },
    },
  },
  args: {
    value: timePresets["2 hours ago"],
  },
  argTypes: {
    value: {
      control: "select",
      options: timePresets,
      description:
        "Date/time value to display relative to now. Select from presets or use the text control to enter a custom ISO string.",
      table: { type: { summary: "string | moment.Moment | Date" } },
    },
    children: {
      control: false,
      description: "Alternative way to provide the value as children. `value` prop takes precedence.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof FromNow>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Interactive relative time display. Use the value dropdown in Controls to change between different time periods (from 30 seconds ago to 1 year ago, plus future times). Hover over the time to see the full date/time in the tooltip.",
      },
    },
  },
};

export const WithChildren: Story = {
  render: () => <FromNow>{localizedMoment().subtract(1, "hour").toISOString()}</FromNow>,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Using the component with children instead of the value prop.",
      },
    },
  },
};

export const VariousTimeframes: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div>
          <strong>Just now:</strong> <FromNow value={localizedMoment().subtract(30, "seconds")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Minutes:</strong> <FromNow value={localizedMoment().subtract(15, "minutes")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Hours:</strong> <FromNow value={localizedMoment().subtract(8, "hours")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Days:</strong> <FromNow value={localizedMoment().subtract(5, "days")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Weeks:</strong> <FromNow value={localizedMoment().subtract(3, "weeks")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Months:</strong> <FromNow value={localizedMoment().subtract(4, "months")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Years:</strong> <FromNow value={localizedMoment().subtract(2, "years")} />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <strong>Future:</strong> <FromNow value={localizedMoment().add(3, "hours")} />
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Various timeframes showing how the relative time changes based on the age of the timestamp.",
      },
    },
  },
};

export const InContext: Story = {
  render: () => (
    <div style={{ padding: "20px" }}>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">System Events</h3>
        </div>
        <ul className="list-group">
          <li className="list-group-item">
            System restarted <FromNow value={localizedMoment().subtract(2, "hours")} />
          </li>
          <li className="list-group-item">
            Package updated <FromNow value={localizedMoment().subtract(1, "day")} />
          </li>
          <li className="list-group-item">
            User logged in <FromNow value={localizedMoment().subtract(3, "days")} />
          </li>
          <li className="list-group-item">
            Configuration changed <FromNow value={localizedMoment().subtract(1, "week")} />
          </li>
        </ul>
      </div>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Common usage showing event timestamps in a list. Hover over times to see the exact date/time.",
      },
    },
  },
};
