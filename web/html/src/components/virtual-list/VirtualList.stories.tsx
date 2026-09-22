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
      <div style={{ padding: "20px" }}>
        <div
          style={{
            height: "600px",
            width: "100%",
            maxWidth: "900px",
            border: "1px solid #ddd",
            borderRadius: "4px",
            overflow: "hidden",
            display: "flex",
          }}
        >
          <Story />
        </div>
      </div>
    ),
  ],
  parameters: {
    docs: {
      description: {
        component:
          "Efficiently renders large item collections by mounting only the visible rows. Virtualizes rendering for performance with large datasets. **Important:** The parent container must have a fixed height constraint.",
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
      description: "Ordered data items to display. An empty array renders no rows or placeholder.",
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

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Interactive virtual list with 100 items. Scroll to see virtualization in action - only visible rows are rendered. Try scrolling quickly to see the performance benefit.",
      },
    },
  },
};

export const ShortCollection: Story = {
  args: {
    items: items.slice(0, 5),
  },
  parameters: {
    docs: {
      description: {
        story: "VirtualList works with small collections too. With only 5 items, all rows fit without scrolling.",
      },
    },
  },
};

export const LargeDataset: Story = {
  args: {
    items: Array.from({ length: 1000 }, (_value, index) => ({
      id: index + 1,
      name: `System ${index + 1}`,
      description: `Environment: ${index % 3 === 0 ? "Production" : index % 3 === 1 ? "Staging" : "Development"}`,
    })),
  },
  parameters: {
    docs: {
      description: {
        story:
          "Virtual list with 1,000 items. Notice how smoothly it scrolls - only ~10 rows are rendered at any time, regardless of the total count.",
      },
    },
  },
};

export const CustomHeight: Story = {
  args: {
    items,
    renderItem: (item) => (
      <div style={{ borderBottom: "1px solid #d8d8d8", padding: "24px" }}>
        <strong style={{ fontSize: "16px" }}>{item.name}</strong>
        <div style={{ marginTop: "8px", color: "#666" }}>{item.description}</div>
        <div style={{ marginTop: "4px", fontSize: "12px", color: "#999" }}>ID: {item.id}</div>
      </div>
    ),
    defaultItemHeight: 95,
  },
  parameters: {
    docs: {
      description: {
        story:
          "Virtual list with taller items. Adjust `defaultItemHeight` to match your item's approximate height for best performance.",
      },
    },
  },
};
