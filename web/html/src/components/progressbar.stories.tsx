import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StorySection } from "manager/storybook/layout";

import { ProgressBar } from "./progressbar";

const meta = {
  title: "Components/Feedback/ProgressBar",
  component: ProgressBar,
  parameters: {
    docs: {
      description: {
        component:
          "Displays numeric progress as a percentage. Incomplete progress is animated; reaching 100 percent removes the active state.",
      },
    },
  },
  args: {
    progress: 60,
    width: "100%",
    title: "60 percent complete",
  },
  argTypes: {
    progress: {
      control: { type: "range", min: 0, max: 100, step: 1 },
      description: "Completion percentage shown as both text and bar width.",
      table: { type: { summary: "number" } },
    },
    width: {
      control: "text",
      description: "CSS width of the progress bar wrapper. Defaults to `100%`.",
      table: { type: { summary: "string" }, defaultValue: { summary: "100%" } },
    },
    title: {
      control: "text",
      description: "Optional native tooltip text for the progress bar.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof ProgressBar>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const ProgressStates: Story = {
  render: () => (
    <StorySection>
      <ProgressBar progress={0} title="Not started" />
      <ProgressBar progress={35} title="In progress" />
      <ProgressBar progress={75} title="Almost complete" />
      <ProgressBar progress={100} title="Complete" />
    </StorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Common progress states from not started to complete." } },
  },
};
