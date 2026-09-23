import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Loading, Spinner } from "./Loading";

const meta = {
  title: "Components/Utilities/Loading",
  component: Loading,
  parameters: {
    docs: {
      description: {
        component:
          "Loading indicator component that displays a spinning icon with optional text. Use to indicate content is being loaded or an operation is in progress. Also exports a standalone `Spinner` component for minimal loading indicators.",
      },
    },
  },
  args: {
    text: "Loading...",
    withBorders: false,
  },
  argTypes: {
    text: {
      control: "text",
      description: "Text displayed below the spinner. Defaults to 'Loading...' if not provided.",
      table: { type: { summary: "string" } },
    },
    withBorders: {
      control: "boolean",
      description: "Show horizontal line separators above and below the loading content.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
  },
} satisfies Meta<typeof Loading>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive loading indicator. Try changing the text and toggling borders.",
      },
    },
  },
};

export const WithBorders: Story = {
  args: {
    text: "Loading data...",
    withBorders: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Loading indicator with horizontal line separators for visual emphasis.",
      },
    },
  },
};

export const SpinnerOnly: Story = {
  render: () => (
    <div style={{ padding: "20px", textAlign: "center" }}>
      <Spinner />
      <p style={{ marginTop: "10px" }}>Standalone spinner without the Loading wrapper</p>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Standalone `Spinner` component for minimal loading indicators without text or borders.",
      },
    },
  },
};
