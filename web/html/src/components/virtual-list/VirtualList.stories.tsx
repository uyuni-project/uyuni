import type { Meta, StoryObj } from "@storybook/react-webpack5";

import VirtualList from "./VirtualList";

type ExampleItem = {
  id: number;
  name: string;
  description: string;
};

const ExampleVirtualList = VirtualList<ExampleItem>;

const items: ExampleItem[] = Array.from({ length: 100 }, (_value, index) => ({
  id: index + 1,
  name: `System ${index + 1}`,
  description: index % 2 === 0 ? "Production" : "Staging",
}));

const meta = {
  title: "Components/Data Display/VirtualList",
  component: ExampleVirtualList,
  decorators: [
    (Story) => (
      <div style={{ display: "flex", height: "320px", maxWidth: "640px" }}>
        <Story />
      </div>
    ),
  ],
  parameters: {
    docs: {
      description: {
        component:
          "Efficiently renders large item collections by mounting only the visible rows. Its parent must provide a constrained height.",
      },
    },
  },
  args: {
    items,
    itemKey: (item) => item.id,
    renderItem: (item) => (
      <div style={{ borderBottom: "1px solid #d8d8d8", padding: "12px" }}>
        <strong>{item.name}</strong>
        <div>{item.description}</div>
      </div>
    ),
    defaultItemHeight: 63,
  },
  argTypes: {
    items: {
      control: "object",
      description: "Ordered data items to display in the virtualized list.",
      table: { type: { summary: "T[]" } },
    },
    renderItem: {
      control: false,
      description: "Function that renders one data item as a list row.",
      table: { type: { summary: "(item: T) => JSX.Element" } },
    },
    itemKey: {
      control: false,
      description: "Function that returns a stable React key for one item.",
      table: { type: { summary: "(item: T) => string | number" } },
    },
    defaultItemHeight: {
      control: { type: "number", min: 1, step: 1 },
      description: "Estimated row height used before the list has measured rendered items.",
      table: { type: { summary: "number" } },
    },
  },
} satisfies Meta<typeof ExampleVirtualList>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Empty: Story = {
  args: {
    items: [],
  },
};

export const ShortCollection: Story = {
  args: {
    items: items.slice(0, 5),
  },
};
