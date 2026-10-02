import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { TabLabel } from "./tab-container";

const meta = {
  title: "Components/Navigation/TabLabel",
  component: TabLabel,
  parameters: {
    docs: {
      description: {
        component:
          "Individual tab navigation label used by `TabContainer`. It renders a list item containing a hash link.",
      },
    },
  },
  decorators: [
    (Story) => (
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          <Story />
        </ul>
      </div>
    ),
  ],
  args: {
    active: false,
    hash: "#systems",
    text: "Systems",
  },
  argTypes: {
    active: {
      control: "boolean",
      description: "Applies the active-tab presentation when enabled.",
    },
    hash: {
      control: "text",
      description: "Hash URL assigned to the tab link. Defaults to `#`.",
    },
    onClick: {
      action: "clicked",
      description: "Optional click handler, normally supplied by `TabContainer`.",
    },
    text: {
      control: "text",
      description: "Content displayed in the tab link.",
    },
  },
} satisfies Meta<typeof TabLabel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <>
      <TabLabel active hash="#active" text="Active tab" />
      <TabLabel hash="#inactive" text="Inactive tab" />
    </>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Active and inactive tab-label presentations." } },
  },
};
