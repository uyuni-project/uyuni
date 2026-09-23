import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SelectSearchField } from "./SelectSearchField";

type SelectSearchFieldProps = {
  label: string;
  criteria?: string;
  options: Array<{ value: string; label: string }>;
  onSearch?: (value: string) => void;
};

const meta = {
  title: "Components/Tables/SelectSearchField",
  component: SelectSearchField,
  decorators: [
    (Story) => (
      <div style={{ minHeight: "400px", maxWidth: "600px" }}>
        <Story />
      </div>
    ),
  ],
  parameters: {
    docs: {
      description: {
        component:
          "A dropdown-based search/filter field typically used in table toolbars. Provides an 'All' option by default and calls `onSearch` when the selection changes. Returns empty string when 'All' is selected.",
      },
    },
  },
  args: {
    label: "Filter by status",
    options: [
      { value: "active", label: "Active" },
      { value: "inactive", label: "Inactive" },
      { value: "pending", label: "Pending" },
    ],
  },
  argTypes: {
    label: {
      control: "text",
      description: "Placeholder text displayed in the dropdown.",
      table: { type: { summary: "string" } },
    },
    criteria: {
      control: "text",
      description: "Initial selected value. If not provided or doesn't match an option, defaults to 'All'.",
      table: { type: { summary: "string" } },
    },
    options: {
      control: "object",
      description: "Array of {value, label} objects for the dropdown options. 'All' is automatically prepended.",
      table: { type: { summary: "Array<{value: string, label: string}>" } },
    },
    onSearch: {
      action: "search triggered",
      description:
        "Callback invoked when selection changes. Receives the selected value (empty string for 'All', otherwise the option value).",
      table: { type: { summary: "(value: string) => void" } },
    },
  },
} satisfies Meta<SelectSearchFieldProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const PlaygroundComponent = (args: SelectSearchFieldProps) => {
  const [selected, setSelected] = useState<string>("");

  return (
    <div style={{ padding: "20px", maxWidth: "400px" }}>
      <SelectSearchField
        {...args}
        onSearch={(value) => {
          setSelected(value);
          action("search triggered")(value);
        }}
      />
      <div style={{ marginTop: "20px", padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Selected value:</strong> {selected || "(All)"}
      </div>
    </div>
  );
};

export const Playground: Story = {
  render: (args) => <PlaygroundComponent {...args} />,
  parameters: {
    docs: {
      description: {
        story:
          "Interactive SelectSearchField. Choose an option to see the search value change. The 'All' option returns an empty string.",
      },
    },
  },
};

const StatusFilterComponent = () => {
  const [searchValue, setSearchValue] = useState<string>("");

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "10px", maxWidth: "300px" }}>
        <SelectSearchField
          label="Status"
          options={[
            { value: "completed", label: "Completed" },
            { value: "queued", label: "Queued" },
            { value: "failed", label: "Failed" },
            { value: "picked-up", label: "Picked Up" },
          ]}
          onSearch={(value) => {
            setSearchValue(value);
            action("status changed")(value);
          }}
        />
      </div>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">Actions</h3>
        </div>
        <div className="panel-body">
          {searchValue ? <p>Showing actions with status: {searchValue}</p> : <p>Showing all actions</p>}
        </div>
      </div>
    </div>
  );
};

export const StatusFilter: Story = {
  render: () => <StatusFilterComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Using SelectSearchField to filter a list of actions by status.",
      },
    },
  },
};

export const WithInitialValue: Story = {
  render: (args) => (
    <div style={{ padding: "20px", maxWidth: "400px" }}>
      <SelectSearchField {...args} />
    </div>
  ),
  args: {
    label: "Filter by type",
    criteria: "security",
    options: [
      { value: "security", label: "Security Updates" },
      { value: "bugfix", label: "Bug Fixes" },
      { value: "enhancement", label: "Enhancements" },
    ],
  },
  parameters: {
    docs: {
      description: {
        story: "SelectSearchField with an initial value set via the criteria prop.",
      },
    },
  },
};

export const ManyOptions: Story = {
  render: () => {
    const countries = [
      { value: "us", label: "United States" },
      { value: "uk", label: "United Kingdom" },
      { value: "ca", label: "Canada" },
      { value: "de", label: "Germany" },
      { value: "fr", label: "France" },
      { value: "it", label: "Italy" },
      { value: "es", label: "Spain" },
      { value: "jp", label: "Japan" },
      { value: "cn", label: "China" },
      { value: "in", label: "India" },
      { value: "br", label: "Brazil" },
      { value: "au", label: "Australia" },
    ];

    return (
      <div style={{ padding: "20px", maxWidth: "400px" }}>
        <SelectSearchField label="Filter by country" options={countries} onSearch={action("country selected")} />
      </div>
    );
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "SelectSearchField with many options. The dropdown is searchable.",
      },
    },
  },
};

const InTableToolbarComponent = () => {
  const [priorityFilter, setPriorityFilter] = useState<string>("");
  const [categoryFilter, setCategoryFilter] = useState<string>("");

  return (
    <div style={{ padding: "20px" }}>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">Issues</h3>
        </div>
        <div className="panel-body">
          <div style={{ display: "flex", gap: "10px", marginBottom: "15px" }}>
            <div style={{ flex: 1 }}>
              <SelectSearchField
                label="Priority"
                options={[
                  { value: "high", label: "High" },
                  { value: "medium", label: "Medium" },
                  { value: "low", label: "Low" },
                ]}
                onSearch={setPriorityFilter}
              />
            </div>
            <div style={{ flex: 1 }}>
              <SelectSearchField
                label="Category"
                options={[
                  { value: "bug", label: "Bug" },
                  { value: "feature", label: "Feature Request" },
                  { value: "task", label: "Task" },
                ]}
                onSearch={setCategoryFilter}
              />
            </div>
          </div>
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Priority</th>
                <th>Category</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td colSpan={4} style={{ textAlign: "center", color: "#999" }}>
                  {priorityFilter || categoryFilter
                    ? `Filtered by: ${[priorityFilter, categoryFilter].filter(Boolean).join(", ")}`
                    : "No filters applied"}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export const InTableToolbar: Story = {
  render: () => <InTableToolbarComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Common usage: multiple SelectSearchFields in a table toolbar for filtering data.",
      },
    },
  },
};
