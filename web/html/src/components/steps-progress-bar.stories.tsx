import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { StepsProgressBar } from "./steps-progress-bar";

const steps = [
  {
    title: "Details",
    content: <p>Review the basic project details.</p>,
  },
  {
    title: "Permissions",
    content: <p>Choose who can access the project.</p>,
  },
  {
    title: "Review",
    content: <p>Confirm the configuration before creating the project.</p>,
  },
];

const meta = {
  title: "Components/Navigation/StepsProgressBar",
  component: StepsProgressBar,
  parameters: {
    docs: {
      description: {
        component:
          "Multi-step workflow navigation that owns the active step, optionally validates before advancing, and exposes cancel and create actions.",
      },
    },
  },
  args: {
    steps,
    onCreate: action("created"),
    onCancel: "#cancel",
  },
  argTypes: {
    steps: {
      control: false,
      description: "Ordered step definitions containing a title, content, and optional validation callback.",
    },
    onCreate: {
      action: "created",
      description: "Called when the user confirms the final step.",
    },
    onCancel: {
      control: "text",
      description: "Destination used by the cancel link.",
    },
  },
} satisfies Meta<typeof StepsProgressBar>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
