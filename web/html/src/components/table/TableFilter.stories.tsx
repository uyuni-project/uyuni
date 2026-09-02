import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { FilterOptionType, TableFilter } from "./TableFilter";

const filterOptions = [
  { label: "Name", value: "name", type: FilterOptionType.TEXT },
  { label: "Package count", value: "packages", type: FilterOptionType.NUMERIC },
  {
    label: "Status",
    value: "status",
    type: FilterOptionType.SELECT,
    filterOptions: [
      { label: "Active", value: "active" },
      { label: "Inactive", value: "inactive" },
    ],
  },
];

type TableFilterProps = React.ComponentProps<typeof TableFilter>;

const StatefulTableFilter = (props: TableFilterProps) => {
  const [field, setField] = useState(props.field);
  const [criteria, setCriteria] = useState(props.criteria);

  useEffect(() => setField(props.field), [props.field]);
  useEffect(() => setCriteria(props.criteria), [props.criteria]);

  return (
    <TableFilter
      {...props}
      field={field}
      criteria={criteria}
      onSearch={(nextCriteria) => {
        setCriteria(nextCriteria ?? "");
        props.onSearch(nextCriteria);
      }}
      onSearchField={(nextField) => {
        setField(nextField);
        setCriteria("");
        props.onSearchField(nextField);
      }}
    />
  );
};

const meta = {
  title: "Components/Table/TableFilter",
  component: TableFilter,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Table filter that switches between text, numeric, and select criteria based on the chosen filter definition.",
      },
    },
  },
  args: {
    filterOptions,
    field: "name",
    criteria: "server",
    placeholder: "Search systems",
    name: "table-filter",
    onSearch: action("criteria changed"),
    onSearchField: action("field changed"),
  },
  argTypes: {
    filterOptions: {
      control: "object",
      description: "Definitions for available filters and their input type.",
      table: { type: { summary: "FilterOption[]" } },
    },
    field: {
      control: "select",
      options: filterOptions.map((option) => option.value),
      description: "Value of the active filter definition.",
      table: { type: { summary: "string" } },
    },
    criteria: {
      control: "text",
      description: "Current criterion passed to the active input.",
      table: { type: { summary: "string" } },
    },
    placeholder: {
      control: "text",
      description: "Placeholder for text-filter input.",
      table: { type: { summary: "string" } },
    },
    name: {
      control: "text",
      description: "HTML name assigned to numeric or text inputs.",
      table: { type: { summary: "string" } },
    },
    onSearch: {
      action: "criteria changed",
      description: "Called when the active filter criterion changes.",
      table: { type: { summary: "(criteria: string | null) => void" } },
    },
    onSearchField: {
      action: "field changed",
      description: "Called when a different filter definition is selected.",
      table: { type: { summary: "(field: string) => void" } },
    },
  },
  render: (args) => <StatefulTableFilter {...args} />,
} satisfies Meta<typeof TableFilter>;

export default meta;

type Story = StoryObj<typeof meta>;

export const TextFilter: Story = {};

export const NumericFilter: Story = {
  args: {
    field: "packages",
    criteria: ">=10",
  },
};

export const SelectFilter: Story = {
  args: {
    field: "status",
    criteria: "active",
  },
};
