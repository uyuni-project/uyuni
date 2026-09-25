import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { ToggleButtonGroup, ToggleButtonOption } from "./toggle-button-group";

type ViewMode = "grid" | "list" | "compact";
type SortOrder = "asc" | "desc";

const viewOptions: ToggleButtonOption<ViewMode>[] = [
  { value: "grid", icon: "fa-th", tooltip: "Grid view" },
  { value: "list", icon: "fa-list", tooltip: "List view" },
  { value: "compact", icon: "fa-th-list", tooltip: "Compact view" },
];

const sortOptions: ToggleButtonOption<SortOrder>[] = [
  { value: "asc", label: "Ascending", icon: "fa-sort-amount-asc" },
  { value: "desc", label: "Descending", icon: "fa-sort-amount-desc" },
];

const StatefulToggleButtonGroup = <T extends string>(props: {
  options: ToggleButtonOption<T>[];
  initialValue: T;
  onChange?: (value: T) => void;
  size?: "sm";
}) => {
  const [value, setValue] = useState<T>(props.initialValue);

  return (
    <div>
      <ToggleButtonGroup
        value={value}
        options={props.options}
        onChange={(newValue) => {
          setValue(newValue);
          action("changed")(newValue);
          props.onChange?.(newValue);
        }}
        size={props.size}
      />
      <p style={{ marginTop: "1rem" }}>
        Selected value: <code>{value}</code>
      </p>
    </div>
  );
};

const meta = {
  title: "Components/Inputs/ToggleButtonGroup",
  component: ToggleButtonGroup,
  parameters: {
    docs: {
      description: {
        component:
          "Toggle button group for selecting a single option from multiple choices. Similar to radio buttons but styled as a button group. The active button is highlighted.",
      },
    },
  },
  args: {
    value: "grid",
    options: viewOptions,
    onChange: action("changed"),
  },
  argTypes: {
    value: {
      control: "text",
      description: "Currently selected value.",
      table: { type: { summary: "string" } },
    },
    options: {
      control: "object",
      description: "Array of button options with value, label, icon, tooltip, and disabled state.",
      table: {
        type: {
          summary: "{ value: string; label?: string; icon?: string; tooltip?: string; disabled?: boolean }[]",
        },
      },
    },
    onChange: {
      action: "changed",
      description: "Callback invoked when a button is clicked. Receives the selected value.",
      table: { type: { summary: "(value: string) => void" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes for the button group container.",
      table: { type: { summary: "string" } },
    },
    size: {
      control: "select",
      options: [undefined, "sm"],
      description: "Button size. Use 'sm' for small buttons.",
      table: { type: { summary: '"sm" | undefined' } },
    },
  },
} satisfies Meta<typeof ToggleButtonGroup>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: () => <StatefulToggleButtonGroup options={viewOptions} initialValue="grid" />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Interactive toggle button group. Click buttons to see the selected value change.",
      },
    },
  },
};

export const IconOnly: Story = {
  render: () => <StatefulToggleButtonGroup options={viewOptions} initialValue="list" />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Toggle buttons with icons only. Hover to see tooltips.",
      },
    },
  },
};

export const WithLabels: Story = {
  render: () => <StatefulToggleButtonGroup options={sortOptions} initialValue="asc" />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Toggle buttons with both icons and text labels.",
      },
    },
  },
};

export const SmallSize: Story = {
  render: () => <StatefulToggleButtonGroup options={viewOptions} initialValue="compact" size="sm" />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Toggle button group with small-sized buttons.",
      },
    },
  },
};

export const WithDisabledOptions: Story = {
  render: () => {
    const options: ToggleButtonOption<string>[] = [
      { value: "read", label: "Read", icon: "fa-eye" },
      { value: "write", label: "Write", icon: "fa-pencil", disabled: true, tooltip: "Write access requires approval" },
      { value: "admin", label: "Admin", icon: "fa-shield", disabled: true, tooltip: "Admin access restricted" },
    ];
    return <StatefulToggleButtonGroup options={options} initialValue="read" />;
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Toggle button group with some options disabled.",
      },
    },
  },
};

export const TextOnly: Story = {
  render: () => {
    const options: ToggleButtonOption<string>[] = [
      { value: "day", label: "Day" },
      { value: "week", label: "Week" },
      { value: "month", label: "Month" },
      { value: "year", label: "Year" },
    ];
    return <StatefulToggleButtonGroup options={options} initialValue="week" />;
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Toggle buttons with text labels only, no icons.",
      },
    },
  },
};

export const VariousContexts: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div>
          <h4>View Mode</h4>
          <StatefulToggleButtonGroup options={viewOptions} initialValue="grid" />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <h4>Sort Order (Small)</h4>
          <StatefulToggleButtonGroup options={sortOptions} initialValue="desc" size="sm" />
        </div>
      </StoryRow>
      <StoryRow>
        <div>
          <h4>Time Range</h4>
          <StatefulToggleButtonGroup
            options={[
              { value: "1h", label: "1 Hour" },
              { value: "24h", label: "24 Hours" },
              { value: "7d", label: "7 Days" },
              { value: "30d", label: "30 Days" },
            ]}
            initialValue="24h"
          />
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Toggle button groups in various usage contexts.",
      },
    },
  },
};
