import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SearchField } from "./SearchField";
import { SearchPanel } from "./SearchPanel";

type SearchPanelProps = React.ComponentProps<typeof SearchPanel>;

const options = [
  { label: "Name", value: "name" },
  { label: "Description", value: "description" },
];

const StatefulSearchPanel = (props: SearchPanelProps) => {
  const [criteria, setCriteria] = useState(props.criteria ?? "");
  const [field, setField] = useState(props.field ?? "name");

  useEffect(() => setCriteria(props.criteria ?? ""), [props.criteria]);
  useEffect(() => setField(props.field ?? "name"), [props.field]);

  return (
    <SearchPanel
      {...props}
      criteria={criteria}
      field={field}
      onSearch={(nextCriteria) => {
        setCriteria(nextCriteria);
        props.onSearch(nextCriteria);
      }}
      onSearchField={(nextField) => {
        setField(nextField);
        props.onSearchField(nextField);
      }}
    />
  );
};

const meta = {
  title: "Components/Table/SearchPanel",
  component: SearchPanel,
  parameters: {
    docs: {
      description: {
        component:
          "Layout and state-propagation wrapper for table search fields. It injects the active field, criterion, and callbacks into each child.",
      },
    },
  },
  args: {
    criteria: "server",
    field: "name",
    searchPanelInline: false,
    onSearch: action("criteria changed"),
    onSearchField: action("field changed"),
    children: <SearchField options={options} placeholder="Search systems" />,
  },
  argTypes: {
    onSearch: {
      action: "criteria changed",
      description: "Called when a child reports a new search criterion.",
      table: { type: { summary: "(criteria: string) => void" } },
    },
    onSearchField: {
      action: "field changed",
      description: "Called when a child reports a new search field.",
      table: { type: { summary: "(field: string) => void" } },
    },
    criteria: {
      control: "text",
      description: "Current search criterion injected into child fields.",
      table: { type: { summary: "string" } },
    },
    field: {
      control: "select",
      options: options.map((option) => option.value),
      description: "Current search-field value injected into child fields.",
      table: { type: { summary: "string" } },
    },
    children: {
      control: false,
      description: "Search-field components that receive the panel's state and callbacks.",
      table: { type: { summary: "ReactNode" } },
    },
    searchPanelInline: {
      control: "boolean",
      description: "Uses the compact inline layout when enabled.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    onClear: {
      control: false,
      description: "Selection-related compatibility callback; it is not rendered directly by `SearchPanel`.",
      table: { type: { summary: "() => void" } },
    },
    onSelectAll: {
      control: false,
      description: "Selection-related compatibility callback; it is not rendered directly by `SearchPanel`.",
      table: { type: { summary: "() => void" } },
    },
    selectedCount: {
      control: false,
      description: "Selection-related compatibility value; it is not rendered directly by `SearchPanel`.",
      table: { type: { summary: "number" } },
    },
    selectable: {
      control: false,
      description: "Selection-related compatibility flag; it is not rendered directly by `SearchPanel`.",
      table: { type: { summary: "boolean" } },
    },
  },
  render: (args) => <StatefulSearchPanel {...args} />,
} satisfies Meta<typeof SearchPanel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Inline: Story = {
  args: {
    searchPanelInline: true,
  },
};
