/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

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
  { id: 1, parentId: null, name: "src", type: "folder" },
  { id: 11, parentId: 1, name: "components", type: "folder" },
  { id: 111, parentId: 11, name: "Button.tsx", type: "file", size: "2.4 KB" },
  { id: 112, parentId: 11, name: "Input.tsx", type: "file", size: "3.1 KB" },
  { id: 12, parentId: 1, name: "utils", type: "folder" },
  { id: 121, parentId: 12, name: "helpers.ts", type: "file", size: "1.8 KB" },
  { id: 2, parentId: null, name: "public", type: "folder" },
  { id: 21, parentId: 2, name: "index.html", type: "file", size: "1.2 KB" },
  { id: 22, parentId: 2, name: "favicon.ico", type: "file", size: "4.5 KB" },
  { id: 3, parentId: null, name: "package.json", type: "file", size: "1.5 KB" },
  { id: 4, parentId: null, name: "README.md", type: "file", size: "3.2 KB" },
];

const meta = {
  title: "Deprecated/Tables/HierarchicalTable",
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
    children: null,
    data: fileSystemData,
    identifier: (row) => row.id,
    expandColumnKey: "name",
    initiallyExpanded: false,
  },
  argTypes: {
    data: {
      control: false,
      description:
        "Flat array of rows with unique ids. Each child refers to its parent through parentId; roots have no parentId or use null.",
    },
    identifier: {
      control: false,
      description: "Function to extract unique identifier from each row.",
    },
    initiallyExpanded: {
      control: "boolean",
      description: "Whether all rows should be expanded initially.",
    },
    expandColumnKey: {
      control: "text",
      description: "Column key where expand/collapse controls should appear.",
    },
    initialSortColumnKey: {
      control: false,
      description: "Sorting is not configured in these deprecated table examples.",
    },
    initialSortDirection: {
      control: false,
      description: "Sorting is not configured in these deprecated table examples.",
    },
    selectable: {
      control: "boolean",
      description: "Enable row selection with checkboxes.",
    },
    onSelect: {
      action: "rows selected",
      description: "Callback when selection changes.",
    },
    indentSize: {
      control: "number",
      description: "Indent size in pixels per nesting level.",
    },
  },
} satisfies Meta<HierarchicalTableProps>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: (args) => (
    <DEPRECATED_HierarchicalTable key={String(args.initiallyExpanded)} {...args}>
      <Column
        columnKey="name"
        header="Name"
        cell={(row: FileNode) => (
          <span>
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
        cell={(row: FileNode) => (
          <span>
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
      { id: 1, parentId: null, name: "Alice Johnson", role: "CEO", department: "Executive" },
      { id: 11, parentId: 1, name: "Bob Smith", role: "CTO", department: "Technology" },
      { id: 111, parentId: 11, name: "Charlie Brown", role: "Lead Developer", department: "Engineering" },
      { id: 112, parentId: 11, name: "Diana Prince", role: "DevOps Engineer", department: "Operations" },
      { id: 12, parentId: 1, name: "Eve Martinez", role: "CFO", department: "Finance" },
      { id: 121, parentId: 12, name: "Frank Miller", role: "Accountant", department: "Accounting" },
    ];

    return (
      <DEPRECATED_HierarchicalTable data={orgData} identifier={(row) => row.id} expandColumnKey="name">
        <Column
          columnKey="name"
          header="Name"
          cell={(row: Employee) => (row.level ? row.name : <strong>{row.name}</strong>)}
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

const WithSelectionComponent = () => {
  const [selectedItems, setSelectedItems] = useState<number[]>([]);

  const simpleData: FileNode[] = [
    { id: 1, parentId: null, name: "Documents", type: "folder" },
    { id: 11, parentId: 1, name: "Report.pdf", type: "file", size: "2.4 MB" },
    { id: 12, parentId: 1, name: "Presentation.pptx", type: "file", size: "5.1 MB" },
    { id: 2, parentId: null, name: "Photo.jpg", type: "file", size: "1.8 MB" },
  ];

  return (
    <DEPRECATED_HierarchicalTable
      data={simpleData}
      identifier={(row) => row.id}
      expandColumnKey="name"
      selectable
      selectedItems={selectedItems}
      onSelect={(items) => {
        setSelectedItems(items);
        action("rows selected")(items);
      }}
    >
      <Column
        columnKey="name"
        header="Name"
        cell={(row: FileNode) => (
          <span>
            <i className={`fa ${row.type === "folder" ? "fa-folder" : "fa-file"}`} style={{ marginRight: "8px" }} />
            {row.name}
          </span>
        )}
      />
      <Column columnKey="size" header="Size" cell={(row: FileNode) => row.size || "-"} />
    </DEPRECATED_HierarchicalTable>
  );
};

export const WithSelection: Story = {
  render: () => <WithSelectionComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Hierarchical table with row selection.",
      },
    },
  },
};
