import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { ExampleSection } from "components/example-layout";

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
    },
    width: {
      control: "text",
      description: "CSS width of the progress bar wrapper. Defaults to `100%`.",
    },
    title: {
      control: "text",
      description: "Optional native tooltip text for the progress bar.",
    },
  },
} satisfies Meta<typeof ProgressBar>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const ProgressStates: Story = {
  render: () => (
    <ExampleSection>
      <ProgressBar progress={0} title="Not started" />
      <ProgressBar progress={35} title="In progress" />
      <ProgressBar progress={75} title="Almost complete" />
      <ProgressBar progress={100} title="Complete" />
    </ExampleSection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Common progress states from not started to complete." } },
  },
};
