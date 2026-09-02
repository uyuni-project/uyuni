import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { DateTime, HumanDateTime } from "./DateTime";

const meta = {
  title: "Components/DateTime/DateTime",
  component: DateTime,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Formats a timestamp using the configured user timezone and date format while retaining a machine-readable ISO value in the `<time>` element.",
      },
    },
  },
  args: {
    value: "2026-09-02T10:30:00Z",
  },
  argTypes: {
    value: {
      control: "text",
      description: "Timestamp to format, supplied as a string or Moment value.",
      table: { type: { summary: "string | moment.Moment" } },
    },
    children: {
      control: "text",
      description: "Alternative string timestamp used when `value` is omitted.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof DateTime>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const HumanReadable: Story = {
  render: (args) => <HumanDateTime {...args} />,
  parameters: {
    docs: {
      description: {
        story: "`HumanDateTime` presents the same timestamp using Moment's calendar-style relative wording.",
      },
    },
  },
};

export const Formats: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <span>
          Exact: <DateTime value="2026-09-02T10:30:00Z" />
        </span>
        <span>
          Calendar: <HumanDateTime value="2026-09-02T10:30:00Z" />
        </span>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Exact and calendar-style formatting of the same timestamp." } },
  },
};
