import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { ExampleRow, StripedExampleSection } from "components/example-layout";

import { DateTime, HumanDateTime } from "./DateTime";

const meta = {
  title: "Components/DateTime/DateTime",
  component: DateTime,
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
    },
    children: {
      control: false,
      description: "Alternative string timestamp used when `value` is omitted.",
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
    <StripedExampleSection>
      <ExampleRow>
        <span>
          Exact: <DateTime value="2026-09-02T10:30:00Z" />
        </span>
        <span>
          Calendar: <HumanDateTime value="2026-09-02T10:30:00Z" />
        </span>
      </ExampleRow>
    </StripedExampleSection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Exact and calendar-style formatting of the same timestamp." } },
  },
};
