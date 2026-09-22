import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { Column } from "./Column";
import { Table } from "./Table";

type TableProps = React.ComponentProps<typeof Table>;

// Sample data
type User = {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
};

const sampleUsers: User[] = [
  { id: 1, name: "Alice Johnson", email: "alice@example.com", role: "Admin", status: "Active" },
  { id: 2, name: "Bob Smith", email: "bob@example.com", role: "User", status: "Active" },
  { id: 3, name: "Charlie Brown", email: "charlie@example.com", role: "User", status: "Inactive" },
  { id: 4, name: "Diana Prince", email: "diana@example.com", role: "Editor", status: "Active" },
  { id: 5, name: "Ethan Hunt", email: "ethan@example.com", role: "User", status: "Active" },
  { id: 6, name: "Fiona Green", email: "fiona@example.com", role: "Admin", status: "Inactive" },
  { id: 7, name: "George Miller", email: "george@example.com", role: "Editor", status: "Active" },
  { id: 8, name: "Hannah Lee", email: "hannah@example.com", role: "User", status: "Active" },
  { id: 9, name: "Ian Malcolm", email: "ian@example.com", role: "User", status: "Active" },
  { id: 10, name: "Julia Roberts", email: "julia@example.com", role: "Admin", status: "Inactive" },
  { id: 11, name: "Kevin Hart", email: "kevin@example.com", role: "User", status: "Active" },
  { id: 12, name: "Laura Palmer", email: "laura@example.com", role: "Editor", status: "Active" },
];

type ServerNode = {
  id: number;
  name: string;
  type: string;
  cpu: number;
  memory: string;
  children?: ServerNode[];
};

const nestedData: ServerNode[] = [
  {
    id: 1,
    name: "Production Cluster",
    type: "Cluster",
    cpu: 64,
    memory: "256GB",
    children: [
      { id: 11, name: "prod-web-01", type: "Web Server", cpu: 8, memory: "32GB" },
      { id: 12, name: "prod-web-02", type: "Web Server", cpu: 8, memory: "32GB" },
      { id: 13, name: "prod-db-01", type: "Database", cpu: 16, memory: "64GB" },
    ],
  },
  {
    id: 2,
    name: "Development Cluster",
    type: "Cluster",
    cpu: 32,
    memory: "128GB",
    children: [
      { id: 21, name: "dev-web-01", type: "Web Server", cpu: 4, memory: "16GB" },
      { id: 22, name: "dev-db-01", type: "Database", cpu: 8, memory: "32GB" },
    ],
  },
  { id: 3, name: "staging-server", type: "Standalone", cpu: 16, memory: "64GB" },
];

const meta = {
  title: "Components/Table/Table",
  component: Table,
  parameters: {
    docs: {
      description: {
        component:
          "A powerful data table component with sorting, filtering, pagination, row selection, expandable rows, and more. Supports both local data arrays and remote data endpoints. Column definitions use the Column component as children.",
      },
    },
  },
  args: {
    data: sampleUsers,
    identifier: (row: User) => row.id,
    initialItemsPerPage: 10,
  },
  argTypes: {
    data: {
      control: false,
      description: "Array of data items or URL endpoint string. Each item represents a row.",
      table: { type: { summary: "any[] | string" } },
    },
    identifier: {
      control: false,
      description: "Function that extracts a unique key from each row object.",
      table: { type: { summary: "(row: any) => any" } },
    },
    initialSortColumnKey: {
      control: "text",
      description: "Column key to sort by initially.",
      table: { type: { summary: "string" } },
    },
    initialSortDirection: {
      control: "select",
      options: [1, -1],
      description: "Initial sort direction: 1 for ascending, -1 for descending.",
      table: { type: { summary: "1 | -1" } },
    },
    initialItemsPerPage: {
      control: "number",
      description: "Number of rows to display per page.",
      table: { type: { summary: "number" } },
    },
    selectable: {
      control: "boolean",
      description: "Enable row selection with checkboxes. Can also be a function for conditional selection.",
      table: { type: { summary: "boolean | ((row: any) => boolean)" } },
    },
    onSelect: {
      action: "rows selected",
      description: "Callback when selection changes. Receives array of selected row identifiers.",
      table: { type: { summary: "(items: any[]) => void" } },
    },
    selectedItems: {
      control: false,
      description: "Array of currently selected row identifiers.",
      table: { type: { summary: "any[]" } },
    },
    onSearch: {
      action: "search",
      description: "Callback when search input changes. Enables search field when provided.",
      table: { type: { summary: "(criteria: string) => void" } },
    },
    searchField: {
      control: false,
      description: "Custom SearchField component for advanced search UI.",
      table: { type: { summary: "ReactElement<SearchField>" } },
    },
    expandable: {
      control: "boolean",
      description: "Enable expandable rows. Rows with `children` array can be expanded.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    deletable: {
      control: "boolean",
      description: "Show delete buttons for rows. Can also be a function for conditional deletion.",
      table: { type: { summary: "boolean | ((row: any) => boolean)" } },
    },
    onDelete: {
      action: "row deleted",
      description: "Callback when delete button is clicked. Receives the row object.",
      table: { type: { summary: "(row: any) => void" } },
    },
    emptyText: {
      control: "text",
      description: "Message to display when there are no rows.",
      table: { type: { summary: "string" }, defaultValue: { summary: "No data available" } },
    },
    loading: {
      control: "boolean",
      description: "Show loading state.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    loadingText: {
      control: "text",
      description: "Message to display while loading.",
      table: { type: { summary: "string" } },
    },
    stickyHeader: {
      control: "boolean",
      description: "Make table header sticky when scrolling.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    titleButtons: {
      control: false,
      description: "Action buttons to display in the table header.",
      table: { type: { summary: "ReactNode[]" } },
    },
    additionalFilters: {
      control: false,
      description: "Additional filter components to display above the table.",
      table: { type: { summary: "ReactNode[]" } },
    },
    children: {
      control: false,
      description: "Column components defining the table structure.",
      table: { type: { summary: "Column[]" } },
    },
  },
} satisfies Meta<TableProps>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: (args) => (
    <Table {...args}>
      <Column columnKey="id" header="ID" cell={(row: User) => row.id} width="10%" />
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} width="25%" />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} width="30%" />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} width="20%" />
      <Column
        columnKey="status"
        header="Status"
        cell={(row: User) => (
          <span className={`label label-${row.status === "Active" ? "success" : "default"}`}>{row.status}</span>
        )}
        width="15%"
      />
    </Table>
  ),
  parameters: {
    docs: {
      description: {
        story:
          "Interactive table with sortable columns. Click column headers to sort. Use the controls to adjust pagination and other settings.",
      },
    },
  },
};

const WithSearchComponent = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const filteredData = sampleUsers.filter(
    (user) =>
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Table
      data={filteredData}
      identifier={(row: User) => row.id}
      initialItemsPerPage={10}
      onSearch={(criteria) => {
        setSearchTerm(criteria);
        action("search")(criteria);
      }}
    >
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
      <Column columnKey="status" header="Status" cell={(row: User) => row.status} />
    </Table>
  );
};

export const WithSearch: Story = {
  render: () => <WithSearchComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Table with search functionality. Type in the search box to filter rows by name or email.",
      },
    },
  },
};

const WithSelectionComponent = () => {
  const [selected, setSelected] = useState<number[]>([]);

  return (
    <div>
      <div style={{ marginBottom: "10px", padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Selected:</strong> {selected.length > 0 ? selected.join(", ") : "None"}
      </div>
      <Table
        data={sampleUsers.slice(0, 6)}
        identifier={(row: User) => row.id}
        selectable
        selectedItems={selected}
        onSelect={(items) => {
          setSelected(items);
          action("rows selected")(items);
        }}
        initialItemsPerPage={10}
      >
        <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
        <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
        <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
      </Table>
    </div>
  );
};

export const WithSelection: Story = {
  render: () => <WithSelectionComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Table with row selection. Check the boxes to select rows. Selected IDs are displayed above the table.",
      },
    },
  },
};

export const ExpandableRows: Story = {
  render: () => (
    <Table data={nestedData} identifier={(row: ServerNode) => row.id} expandable initialItemsPerPage={20}>
      <Column
        columnKey="name"
        header="Name"
        cell={(row: ServerNode, _criteria, nestingLevel) => (nestingLevel ? row.name : <strong>{row.name}</strong>)}
        width="30%"
      />
      <Column columnKey="type" header="Type" cell={(row: ServerNode) => row.type} width="20%" />
      <Column columnKey="cpu" header="CPU Cores" cell={(row: ServerNode) => row.cpu} width="20%" />
      <Column columnKey="memory" header="Memory" cell={(row: ServerNode) => row.memory} width="30%" />
    </Table>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Table with expandable rows. Click the chevron icon to expand/collapse rows that have children. Parent rows are bold.",
      },
    },
  },
};

const WithDeletionComponent = () => {
  const [data, setData] = useState(sampleUsers.slice(0, 5));

  return (
    <Table
      data={data}
      identifier={(row: User) => row.id}
      deletable
      onDelete={(row) => {
        setData(data.filter((user) => user.id !== row.id));
        action("row deleted")(row);
      }}
      initialItemsPerPage={10}
    >
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
    </Table>
  );
};

export const WithDeletion: Story = {
  render: () => <WithDeletionComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Table with delete buttons. Click the trash icon to remove a row.",
      },
    },
  },
};

export const EmptyState: Story = {
  render: (args) => (
    <Table {...args} data={[]} emptyText="No users found. Try adding some users to get started.">
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
    </Table>
  ),
  parameters: {
    docs: {
      description: {
        story: "Table with no data. Shows the empty state message.",
      },
    },
  },
};

export const LoadingState: Story = {
  render: (args) => (
    <Table {...args} data={[]} loading loadingText="Loading users...">
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
    </Table>
  ),
  parameters: {
    docs: {
      description: {
        story: "Table in loading state. Shows a loading message.",
      },
    },
  },
};

const WithTitleButtonsComponent = () => {
  const [selected, setSelected] = useState<number[]>([]);

  const actionButtons = [
    <Button key="add" className="btn-success" icon="fa-plus" text="Add User" handler={() => action("add clicked")()} />,
    <Button
      key="delete"
      className="btn-danger"
      icon="fa-trash"
      text={`Delete (${selected.length})`}
      disabled={selected.length === 0}
      handler={() => action("delete clicked")(selected)}
    />,
  ];

  return (
    <Table
      data={sampleUsers.slice(0, 6)}
      identifier={(row: User) => row.id}
      selectable
      selectedItems={selected}
      onSelect={setSelected}
      titleButtons={actionButtons}
      initialItemsPerPage={10}
    >
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
    </Table>
  );
};

export const WithTitleButtons: Story = {
  render: () => <WithTitleButtonsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Table with action buttons in the header. Bulk operations like Add and Delete appear at the top.",
      },
    },
  },
};

export const StickyHeader: Story = {
  render: (args) => (
    <div style={{ maxHeight: "400px", overflow: "auto", border: "1px solid #ddd" }}>
      <Table {...args} data={sampleUsers} stickyHeader initialItemsPerPage={20}>
        <Column columnKey="id" header="ID" cell={(row: User) => row.id} />
        <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
        <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
        <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
        <Column columnKey="status" header="Status" cell={(row: User) => row.status} />
      </Table>
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "Table with sticky header. Scroll down to see the header stay at the top.",
      },
    },
  },
};

export const InitialSort: Story = {
  render: (args) => (
    <Table {...args} initialSortColumnKey="name" initialSortDirection={1}>
      <Column columnKey="id" header="ID" cell={(row: User) => row.id} sortable />
      <Column columnKey="name" header="Name (Initially Sorted)" cell={(row: User) => row.name} sortable />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} sortable />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} sortable />
    </Table>
  ),
  args: {
    data: sampleUsers,
    identifier: (row: User) => row.id,
    initialItemsPerPage: 10,
  },
  parameters: {
    docs: {
      description: {
        story:
          "Table with initial sort applied to the Name column in ascending order. Click any column header to change sorting.",
      },
    },
  },
};

const ConditionalSelectionComponent = () => {
  const [selected, setSelected] = useState<number[]>([]);

  return (
    <div>
      <div style={{ marginBottom: "10px", padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>Note:</strong> Only Active users can be selected
      </div>
      <Table
        data={sampleUsers.slice(0, 8)}
        identifier={(row: User) => row.id}
        selectable={(row: User) => row.status === "Active"}
        selectedItems={selected}
        onSelect={setSelected}
        initialItemsPerPage={10}
      >
        <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
        <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
        <Column columnKey="status" header="Status" cell={(row: User) => row.status} />
      </Table>
    </div>
  );
};

export const ConditionalSelection: Story = {
  render: () => <ConditionalSelectionComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Table with conditional row selection. Only rows where status is 'Active' can be selected. Inactive rows show disabled checkboxes.",
      },
    },
  },
};

export const Pagination: Story = {
  render: (args) => (
    <Table {...args} data={sampleUsers} initialItemsPerPage={5}>
      <Column columnKey="id" header="ID" cell={(row: User) => row.id} />
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} />
    </Table>
  ),
  parameters: {
    docs: {
      description: {
        story:
          "Table with pagination showing 5 items per page. Use the pagination controls at the bottom to navigate pages and adjust items per page.",
      },
    },
  },
};

const CompleteExampleComponent = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [selected, setSelected] = useState<number[]>([]);
  const [data, setData] = useState(sampleUsers);

  const filteredData = data.filter(
    (user) =>
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.role.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const actionButtons = [
    <Button
      key="export"
      className="btn-default"
      icon="fa-download"
      text="Export"
      handler={() => action("export clicked")(selected)}
    />,
    <Button
      key="delete"
      className="btn-danger"
      icon="fa-trash"
      text={`Delete (${selected.length})`}
      disabled={selected.length === 0}
      handler={() => {
        setData(data.filter((user) => !selected.includes(user.id)));
        setSelected([]);
        action("bulk delete")(selected);
      }}
    />,
  ];

  return (
    <Table
      data={filteredData}
      identifier={(row: User) => row.id}
      onSearch={setSearchTerm}
      selectable
      selectedItems={selected}
      onSelect={setSelected}
      deletable
      onDelete={(row) => {
        setData(data.filter((user) => user.id !== row.id));
        action("row deleted")(row);
      }}
      titleButtons={actionButtons}
      initialItemsPerPage={5}
      initialSortColumnKey="name"
      emptyText="No users match your search criteria"
    >
      <Column columnKey="id" header="ID" cell={(row: User) => row.id} sortable width="10%" />
      <Column columnKey="name" header="Name" cell={(row: User) => row.name} sortable width="25%" />
      <Column columnKey="email" header="Email" cell={(row: User) => row.email} sortable width="30%" />
      <Column columnKey="role" header="Role" cell={(row: User) => row.role} sortable width="20%" />
      <Column
        columnKey="status"
        header="Status"
        cell={(row: User) => (
          <span className={`label label-${row.status === "Active" ? "success" : "default"}`}>{row.status}</span>
        )}
        sortable
        width="15%"
      />
    </Table>
  );
};

export const CompleteExample: Story = {
  render: () => <CompleteExampleComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Complete example combining search, selection, deletion, sorting, pagination, and bulk actions. Demonstrates all major Table features working together.",
      },
    },
  },
};
