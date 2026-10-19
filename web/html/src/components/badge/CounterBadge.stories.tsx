import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";
import { BootstrapPanel } from "components/panels/BootstrapPanel";

import { CounterBadge } from "./CounterBadge";

const meta = {
  title: "Components/Badge/CounterBadge",
  component: CounterBadge,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Displays a numeric value associated with another UI element, such as a button, tab, or navigation item. Use it only for numeric values, keep the count concise (prefer `99+` over large numbers), and place it close to the element it describes.",
      },
    },
  },
  args: {
    count: "1",
    status: "default",
  },
  argTypes: {
    count: {
      control: "text",
      description: "The value to display. This is a string so that truncated counts such as `999+` are possible.",
    },
    status: {
      control: "inline-radio",
      options: ["default", "highlight"],
    },
  },
} satisfies Meta<typeof CounterBadge>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Use the controls to try out `count` and `status`.",
      },
    },
  },
};

export const Statuses: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Use `default` for ordinary counts and `highlight` to draw attention to a count that needs the user's action.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <CounterBadge count="1" />
        <CounterBadge count="999+" status="highlight" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const CountLengths: Story = {
  name: "Count lengths",
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "The badge grows with its content. Truncate large numbers rather than letting the badge dominate the UI.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <CounterBadge count="1" />
        <CounterBadge count="42" />
        <CounterBadge count="99+" />
        <CounterBadge count="999+" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const InContext: Story = {
  name: "In context",
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Place the badge next to the element whose count it describes.",
      },
    },
  },
  render: () => (
    <BootstrapPanel title="Panel">
      <StoryRow>
        <span>
          Systems <CounterBadge count="12" />
        </span>
        <span>
          Pending actions <CounterBadge count="3" status="highlight" />
        </span>
        <Button className="btn-default" text="Notifications" icon="fa-bell" />
        <CounterBadge count="999+" status="highlight" />
      </StoryRow>
    </BootstrapPanel>
  ),
};
