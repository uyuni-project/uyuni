import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SearchField } from "./SearchField";

type SearchFieldProps = React.ComponentProps<typeof SearchField>;

const searchOptions = [
  { label: "Name", value: "name" },
  { label: "Operating system", value: "os" },
  { label: "Description", value: "description" },
];

const StatefulSearchField = (props: SearchFieldProps) => {
  const [criteria, setCriteria] = useState(props.criteria ?? "");
  const [field, setField] = useState(props.field ?? "");

  useEffect(() => setCriteria(props.criteria ?? ""), [props.criteria]);
  useEffect(() => setField(props.field ?? ""), [props.field]);

  return (
    <SearchField
      {...props}
      criteria={criteria}
      field={field}
      onSearch={(nextCriteria) => {
        setCriteria(nextCriteria);
        props.onSearch?.(nextCriteria);
      }}
      onSearchField={(nextField) => {
        setField(nextField);
        props.onSearchField?.(nextField);
      }}
    />
  );
};

const meta = {
  title: "Components/Table/SearchField",
  component: SearchField,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Controlled text search for tables, optionally paired with a dropdown that selects the field to search.",
      },
    },
  },
  args: {
    criteria: "server",
    field: "name",
    options: searchOptions,
    placeholder: "Search systems",
    name: "system-search",
    onSearch: action("criteria changed"),
    onSearchField: action("field changed"),
  },
  argTypes: {
    criteria: {
      control: "text",
      description: "Current search text.",
      table: { type: { summary: "string" } },
    },
    field: {
      control: "select",
      options: searchOptions.map((option) => option.value),
      description: "Value of the currently selected search field.",
      table: { type: { summary: "string" } },
    },
    options: {
      control: "object",
      description: "Search-field options shown before the text input.",
      table: { type: { summary: "{ label: string; value: string }[]" } },
    },
    placeholder: {
      control: "text",
      description: "Hint displayed while the search text is empty.",
      table: { type: { summary: "string" }, defaultValue: { summary: "Search" } },
    },
    onSearch: {
      action: "criteria changed",
      description: "Called whenever the search text changes.",
      table: { type: { summary: "(criteria: string) => void" } },
    },
    onSearchField: {
      action: "field changed",
      description: "Called whenever a different search field is selected.",
      table: { type: { summary: "(field: string) => void" } },
    },
    filter: {
      control: false,
      description: "Optional row-filtering function consumed by the table data handler rather than rendered here.",
      table: { type: { summary: "(datum: any, criteria?: string) => boolean" } },
    },
    name: {
      control: "text",
      description: "HTML name assigned to the text input.",
      table: { type: { summary: "string" } },
    },
  },
  render: (args) => <StatefulSearchField {...args} />,
} satisfies Meta<typeof SearchField>;

export default meta;

type Story = StoryObj<typeof meta>;

export const WithFieldSelector: Story = {};

export const TextOnly: Story = {
  args: {
    options: undefined,
    field: undefined,
    criteria: "",
  },
};
