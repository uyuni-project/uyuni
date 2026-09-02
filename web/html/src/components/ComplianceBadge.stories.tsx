import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { ComplianceBadge } from "./ComplianceBadge";

const meta = {
  title: "Components/Feedback/ComplianceBadge",
  component: ComplianceBadge,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Displays the result of compliance scans as a compact, color-coded badge. Percentages below 50 are dangerous, percentages from 50 to 79 are warnings, and percentages of 80 or more are successful.",
      },
    },
  },
  args: {
    percentage: 85,
    compliant: 17,
    total: 20,
  },
  argTypes: {
    percentage: {
      control: { type: "number", min: 0, max: 100, step: 1 },
      description: "Compliance percentage used to select the badge color.",
      table: { type: { summary: "number" } },
    },
    compliant: {
      control: { type: "number", min: 0, step: 1 },
      description: "Number of compliant scans displayed before the total.",
      table: { type: { summary: "number" } },
    },
    total: {
      control: { type: "number", min: 0, step: 1 },
      description: "Total number of scans. A value of zero displays the no-scans state.",
      table: { type: { summary: "number" } },
    },
  },
} satisfies Meta<typeof ComplianceBadge>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Thresholds: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <ComplianceBadge percentage={0} compliant={0} total={0} />
        <ComplianceBadge percentage={30} compliant={3} total={10} />
        <ComplianceBadge percentage={60} compliant={6} total={10} />
        <ComplianceBadge percentage={80} compliant={8} total={10} />
        <ComplianceBadge percentage={100} compliant={10} total={10} />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "The no-scans state and the danger, warning, and success thresholds.",
      },
    },
  },
};
