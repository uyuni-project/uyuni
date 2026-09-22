import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Column } from "./Column";
import { DEPRECATED_HierarchicalTable, HierarchicalRow } from "./HierarchicalTable";

type HierarchicalTableProps = React.ComponentProps<typeof DEPRECATED_HierarchicalTable>;

type FileNode = HierarchicalRow & {
  name: string;
  type: string;
  size?: string;
};

const fileSystemData: FileNode[] = [
  {
    id: 1,
    parentId: null,
    name: "src",
    type: "folder",
    children: [
      {
        id: 11,
        parentId: 1,
        name: "components",
        type: "folder",
        children: [
          { id: 111, parentId: 11, name: "Button.tsx", type: "file", size: "2.4 KB" },
          { id: 112, parentId: 11, name: "Input.tsx", type: "file", size: "3.1 KB" },
        ],
      },
      {
        id: 12,
        parentId: 1,
        name: "utils",
        type: "folder",
        children: [{ id: 121, parentId: 12, name: "helpers.ts", type: "file", size: "1.8 KB" }],
      },
    ],
  },
  {
    id: 2,
    parentId: null,
    name: "public",
    type: "folder",
    children: [
      { id: 21, parentId: 2, name: "index.html", type: "file", size: "1.2 KB" },
      { id: 22, parentId: 2, name: "favicon.ico", type: "file", size: "4.5 KB" },
    ],
  },
  { id: 3, parentId: null, name: "package.json", type: "file", size: "1.5 KB" },
  { id: 4, parentId: null, name: "README.md", type: "file", size: "3.2 KB" },
];

const meta = {
  title: "Components/DEPRECATED/HierarchicalTable",
  component: DEPRECATED_HierarchicalTable,
  parameters: {
    docs: {
      description: {
        component: `
**⚠️ DEPRECATED - Do not use in new code**

This component is deprecated. Use \`Table\` with \`expandable\` prop instead.

Example replacement:
\`\`\`tsx
<Table data={data} identifier={(row) => row.id} expandable>
  <Column columnKey="name" header="Name" cell={(row) => row.name} />
</Table>
\`\`\`

The HierarchicalTable component has complex internal state management and performance issues.
The new Table component with expandable rows is simpler, faster, and better integrated with React.
        `,
      },
    },
  },
  args: {
    data: fileSystemData,
    identifier: (row: FileNode) => row.id,
    initiallyExpanded: false,
  },
  argTypes: {
    data: {
      control: false,
      description: "Array of hierarchical data. Each item should have id, parentId, and optional children array.",
      table: { type: { summary: "HierarchicalRow[]" } },
    },
    identifier: {
      control: false,
      description: "Function to extract unique identifier from each row.",
      table: { type: { summary: "(row: HierarchicalRow) => string | number" } },
    },
    initiallyExpanded: {
      control: "boolean",
      description: "Whether all rows should be expanded initially.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    expandColumnKey: {
      control: "text",
      description: "Column key where expand/collapse controls should appear.",
      table: { type: { summary: "string" } },
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
    selectable: {
      control: "boolean",
      description: "Enable row selection with checkboxes.",
      table: { type: { summary: "boolean | ((row: any) => boolean)" } },
    },
    onSelect: {
      action: "rows selected",
      description: "Callback when selection changes.",
      table: { type: { summary: "(items: any[]) => void" } },
    },
    indentSize: {
      control: "number",
      description: "Indent size in pixels per nesting level.",
      table: { type: { summary: "number" }, defaultValue: { summary: "20" } },
    },
  },
} satisfies Meta<HierarchicalTableProps>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: (args) => (
    <DEPRECATED_HierarchicalTable {...args}>
      <Column
        columnKey="name"
        header="Name"
        cell={(row: FileNode, _criteria, nestingLevel) => (
          <span style={{ paddingLeft: `${(nestingLevel || 0) * 20}px` }}>
            <i className={`fa ${row.type === "folder" ? "fa-folder" : "fa-file"}`} style={{ marginRight: "8px" }} />
            {row.name}
          </span>
        )}
        width="50%"
      />
      <Column columnKey="type" header="Type" cell={(row: FileNode) => row.type} width="25%" />
      <Column columnKey="size" header="Size" cell={(row: FileNode) => row.size || "-"} width="25%" />
    </DEPRECATED_HierarchicalTable>
  ),
  parameters: {
    docs: {
      description: {
        story:
          "⚠️ DEPRECATED - File system hierarchy table. Click the chevron icons to expand/collapse. Use Table with expandable for new code.",
      },
    },
  },
};

export const InitiallyExpanded: Story = {
  render: (args) => (
    <DEPRECATED_HierarchicalTable {...args} initiallyExpanded>
      <Column
        columnKey="name"
        header="Name"
        cell={(row: FileNode, _criteria, nestingLevel) => (
          <span style={{ paddingLeft: `${(nestingLevel || 0) * 20}px` }}>
            <i className={`fa ${row.type === "folder" ? "fa-folder" : "fa-file"}`} style={{ marginRight: "8px" }} />
            {row.name}
          </span>
        )}
      />
      <Column columnKey="type" header="Type" cell={(row: FileNode) => row.type} />
      <Column columnKey="size" header="Size" cell={(row: FileNode) => row.size || "-"} />
    </DEPRECATED_HierarchicalTable>
  ),
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - All rows expanded by default.",
      },
    },
  },
};

export const OrganizationChart: Story = {
  render: () => {
    type Employee = HierarchicalRow & {
      name: string;
      role: string;
      department: string;
    };

    const orgData: Employee[] = [
      {
        id: 1,
        parentId: null,
        name: "Alice Johnson",
        role: "CEO",
        department: "Executive",
        children: [
          {
            id: 11,
            parentId: 1,
            name: "Bob Smith",
            role: "CTO",
            department: "Technology",
            children: [
              { id: 111, parentId: 11, name: "Charlie Brown", role: "Lead Developer", department: "Engineering" },
              { id: 112, parentId: 11, name: "Diana Prince", role: "DevOps Engineer", department: "Operations" },
            ],
          },
          {
            id: 12,
            parentId: 1,
            name: "Eve Martinez",
            role: "CFO",
            department: "Finance",
            children: [{ id: 121, parentId: 12, name: "Frank Miller", role: "Accountant", department: "Accounting" }],
          },
        ],
      },
    ];

    return (
      <DEPRECATED_HierarchicalTable data={orgData} identifier={(row: Employee) => row.id}>
        <Column
          columnKey="name"
          header="Name"
          cell={(row: Employee, _criteria, nestingLevel) => (
            <span style={{ paddingLeft: `${(nestingLevel || 0) * 20}px` }}>
              {nestingLevel ? row.name : <strong>{row.name}</strong>}
            </span>
          )}
          width="35%"
        />
        <Column columnKey="role" header="Role" cell={(row: Employee) => row.role} width="30%" />
        <Column columnKey="department" header="Department" cell={(row: Employee) => row.department} width="35%" />
      </DEPRECATED_HierarchicalTable>
    );
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Organization hierarchy showing reporting structure.",
      },
    },
  },
};

export const WithSelection: Story = {
  render: () => {
    const simpleData: FileNode[] = [
      {
        id: 1,
        parentId: null,
        name: "Documents",
        type: "folder",
        children: [
          { id: 11, parentId: 1, name: "Report.pdf", type: "file", size: "2.4 MB" },
          { id: 12, parentId: 1, name: "Presentation.pptx", type: "file", size: "5.1 MB" },
        ],
      },
      { id: 2, parentId: null, name: "Photo.jpg", type: "file", size: "1.8 MB" },
    ];

    return (
      <DEPRECATED_HierarchicalTable
        data={simpleData}
        identifier={(row: FileNode) => row.id}
        selectable
        onSelect={action("rows selected")}
      >
        <Column
          columnKey="name"
          header="Name"
          cell={(row: FileNode, _criteria, nestingLevel) => (
            <span style={{ paddingLeft: `${(nestingLevel || 0) * 20}px` }}>
              <i className={`fa ${row.type === "folder" ? "fa-folder" : "fa-file"}`} style={{ marginRight: "8px" }} />
              {row.name}
            </span>
          )}
        />
        <Column columnKey="size" header="Size" cell={(row: FileNode) => row.size || "-"} />
      </DEPRECATED_HierarchicalTable>
    );
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Hierarchical table with row selection.",
      },
    },
  },
};
