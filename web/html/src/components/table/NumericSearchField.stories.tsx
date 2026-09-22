import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { NumericSearchField } from "./NumericSearchField";

type NumericSearchFieldProps = {
  name: string;
  criteria?: string;
  onSearch: (criteria: string | null) => void;
};

const meta = {
  title: "Components/Table/NumericSearchField",
  component: NumericSearchField,
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
          "A numeric comparison search field typically used in table filters. Combines a matcher dropdown (less than, equal to, greater than, etc.) with a numeric input. Returns a criteria string like '>=100' or '<50', or null if no value is entered.",
      },
    },
  },
  args: {
    name: "numeric-search",
  },
  argTypes: {
    name: {
      control: "text",
      description: "HTML name attribute for the numeric input field.",
      table: { type: { summary: "string" } },
    },
    criteria: {
      control: "text",
      description:
        "Initial criteria value in the format 'matcher+value' (e.g., '>=100', '<50', '=42'). The component will parse this into matcher and value.",
      table: { type: { summary: "string" } },
    },
    onSearch: {
      action: "search triggered",
      description:
        "Callback invoked when matcher or value changes. Receives a criteria string (e.g., '>=100') or null if value is empty.",
      table: { type: { summary: "(criteria: string | null) => void" } },
    },
  },
} satisfies Meta<NumericSearchFieldProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const PlaygroundComponent = (args: NumericSearchFieldProps) => {
  const [criteria, setCriteria] = useState<string | null>(null);

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "20px" }}>
        <NumericSearchField
          {...args}
          onSearch={(value) => {
            setCriteria(value);
            action("search triggered")(value);
          }}
        />
      </div>
      <div style={{ padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Current criteria:</strong> {criteria || "(none)"}
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
          "Interactive NumericSearchField. Select a matcher (less than, equal to, etc.) and enter a number to see the resulting criteria string.",
      },
    },
  },
};

const MemoryFilterComponent = () => {
  const [searchCriteria, setSearchCriteria] = useState<string | null>(null);

  return (
    <div style={{ padding: "20px" }}>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">Server Memory Filter</h3>
        </div>
        <div className="panel-body">
          <div style={{ marginBottom: "15px" }}>
            <div style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>Memory (GB)</div>
            <NumericSearchField
              name="memory"
              onSearch={(criteria) => {
                setSearchCriteria(criteria);
                action("memory filter changed")(criteria);
              }}
            />
          </div>
          <div style={{ padding: "10px", background: "#f0f8ff", borderRadius: "4px" }}>
            {searchCriteria ? (
              <p>
                Showing servers with memory <code>{searchCriteria}</code> GB
              </p>
            ) : (
              <p>Showing all servers</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export const MemoryFilter: Story = {
  render: () => <MemoryFilterComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Using NumericSearchField to filter servers by memory size.",
      },
    },
  },
};

export const WithInitialCriteria: Story = {
  render: () => (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "10px" }}>
        <strong>Preset: Greater than or equal to 8</strong>
      </div>
      <NumericSearchField name="preset-example" criteria=">=8" onSearch={action("search")} />
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "NumericSearchField with initial criteria value. The component parses '>=8' into matcher and value.",
      },
    },
  },
};

export const AllMatchers: Story = {
  render: () => {
    const matchers = [
      { criteria: "<10", description: "Less than 10" },
      { criteria: "<=10", description: "Less than or equal to 10" },
      { criteria: "=10", description: "Equal to 10" },
      { criteria: ">=10", description: "Greater than or equal to 10" },
      { criteria: ">10", description: "Greater than 10" },
      { criteria: "!=10", description: "Not equal to 10" },
    ];

    return (
      <div style={{ padding: "20px" }}>
        <h4 style={{ marginBottom: "15px" }}>All available matchers:</h4>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))", gap: "15px" }}>
          {matchers.map(({ criteria, description }) => (
            <div key={criteria} style={{ border: "1px solid #ddd", borderRadius: "4px", padding: "15px" }}>
              <div style={{ marginBottom: "10px", fontWeight: "bold" }}>{description}</div>
              <NumericSearchField name={`matcher-${criteria}`} criteria={criteria} onSearch={action("search")} />
            </div>
          ))}
        </div>
      </div>
    );
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "All six comparison matchers supported by NumericSearchField.",
      },
    },
  },
};

const MultipleFiltersComponent = () => {
  const [cpuCriteria, setCpuCriteria] = useState<string | null>(null);
  const [ramCriteria, setRamCriteria] = useState<string | null>(null);
  const [diskCriteria, setDiskCriteria] = useState<string | null>(null);

  return (
    <div style={{ padding: "20px" }}>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">Hardware Specifications Filter</h3>
        </div>
        <div className="panel-body">
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "15px", marginBottom: "15px" }}>
            <div>
              <div style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>CPU Cores</div>
              <NumericSearchField name="cpu" onSearch={setCpuCriteria} />
            </div>
            <div>
              <div style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>RAM (GB)</div>
              <NumericSearchField name="ram" onSearch={setRamCriteria} />
            </div>
            <div>
              <div style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>Disk (TB)</div>
              <NumericSearchField name="disk" onSearch={setDiskCriteria} />
            </div>
          </div>
          <div style={{ padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
            <strong>Active filters:</strong>
            <ul style={{ marginBottom: 0, marginTop: "5px" }}>
              {cpuCriteria && (
                <li>
                  CPU: <code>{cpuCriteria}</code> cores
                </li>
              )}
              {ramCriteria && (
                <li>
                  RAM: <code>{ramCriteria}</code> GB
                </li>
              )}
              {diskCriteria && (
                <li>
                  Disk: <code>{diskCriteria}</code> TB
                </li>
              )}
              {!cpuCriteria && !ramCriteria && !diskCriteria && <li>None</li>}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export const MultipleFilters: Story = {
  render: () => <MultipleFiltersComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Multiple NumericSearchFields working together to filter hardware specifications.",
      },
    },
  },
};

const InTableContextComponent = () => {
  const [ageCriteria, setAgeCriteria] = useState<string | null>(null);

  return (
    <div style={{ padding: "20px" }}>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Name</th>
            <th>
              <div>Age</div>
              <NumericSearchField name="age-filter" onSearch={setAgeCriteria} />
            </th>
            <th>Email</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td colSpan={3} style={{ textAlign: "center", color: "#999" }}>
              {ageCriteria ? `Filtering by age: ${ageCriteria}` : "No age filter applied"}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};

export const InTableContext: Story = {
  render: () => <InTableContextComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "NumericSearchField integrated directly into table header for inline filtering.",
      },
    },
  },
};
