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
      key={`${field}-${props.options ? "with-options" : "text-only"}`}
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
  title: "Components/Tables/SearchField",
  component: SearchField,
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
    },
    field: {
      control: "select",
      options: searchOptions.map((option) => option.value),
      description: "Value of the currently selected search field.",
    },
    options: {
      control: "object",
      description: "Search-field options shown before the text input.",
    },
    placeholder: {
      control: "text",
      description: "Hint displayed while the search text is empty.",
    },
    onSearch: {
      action: "criteria changed",
      description: "Called whenever the search text changes.",
    },
    onSearchField: {
      action: "field changed",
      description: "Called whenever a different search field is selected.",
    },
    filter: {
      control: false,
      description: "Optional row-filtering function consumed by the table data handler rather than rendered here.",
    },
    name: {
      control: "text",
      description: "HTML name assigned to the text input.",
    },
  },
  render: (args) => <StatefulSearchField {...args} />,
} satisfies Meta<typeof SearchField>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const TextOnly: Story = {
  args: {
    options: undefined,
    field: undefined,
    criteria: "",
  },
};
