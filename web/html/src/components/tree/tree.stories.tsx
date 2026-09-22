import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { CustomDiv } from "components/custom-objects";

import { Tree, TreeData, TreeItem } from "./tree";

const simpleTreeData: TreeData = {
  rootId: "root",
  items: [
    { id: "root", children: ["folder1", "folder2", "file1"] },
    { id: "folder1", data: { name: "Documents", type: "folder" }, children: ["file2", "file3"] },
    { id: "folder2", data: { name: "Images", type: "folder" }, children: ["file4", "folder3"] },
    { id: "folder3", data: { name: "Vacation", type: "folder" }, children: ["file5", "file6"] },
    { id: "file1", data: { name: "README.txt", type: "file", size: "2 KB" } },
    { id: "file2", data: { name: "invoice.pdf", type: "file", size: "156 KB" } },
    { id: "file3", data: { name: "report.docx", type: "file", size: "45 KB" } },
    { id: "file4", data: { name: "photo.jpg", type: "file", size: "2.3 MB" } },
    { id: "file5", data: { name: "beach.jpg", type: "file", size: "3.1 MB" } },
    { id: "file6", data: { name: "sunset.jpg", type: "file", size: "2.8 MB" } },
  ],
};

const organizationTreeData: TreeData = {
  rootId: "root",
  items: [
    { id: "root", children: ["eng", "sales", "hr"] },
    { id: "eng", data: { name: "Engineering", count: 15 }, children: ["frontend", "backend"] },
    { id: "sales", data: { name: "Sales", count: 8 } },
    { id: "hr", data: { name: "Human Resources", count: 5 } },
    { id: "frontend", data: { name: "Frontend Team", count: 6 } },
    { id: "backend", data: { name: "Backend Team", count: 9 }, children: ["api", "database"] },
    { id: "api", data: { name: "API Team", count: 5 } },
    { id: "database", data: { name: "Database Team", count: 4 } },
  ],
};

const meta = {
  title: "Components/Data Display/Tree",
  component: Tree,
  parameters: {
    docs: {
      description: {
        component:
          "Hierarchical tree view component with expandable/collapsible nodes. Supports optional checkboxes for multi-selection and custom rendering of tree items.",
      },
    },
  },
  args: {
    data: simpleTreeData,
    initiallyExpanded: [],
    initiallySelected: [],
  },
  argTypes: {
    data: {
      control: "object",
      description: "Tree data structure containing items and root ID.",
      table: {
        type: {
          summary: "{ rootId: string; items: TreeItem[] }",
        },
      },
    },
    renderItem: {
      control: false,
      description:
        "Function to render each tree item. Receives the item and a renderNameColumn helper for the expandable name column.",
      table: { type: { summary: "(item: TreeItem, renderNameColumn: (name: ReactNode) => ReactNode) => ReactNode" } },
    },
    header: {
      control: false,
      description: "Optional header content displayed above the tree.",
      table: { type: { summary: "ReactNode" } },
    },
    initiallyExpanded: {
      control: "object",
      description: "Array of item IDs that should be expanded when the tree first renders.",
      table: { type: { summary: "string[]" } },
    },
    initiallySelected: {
      control: "object",
      description: "Array of item IDs that should be selected (checked) when the tree first renders.",
      table: { type: { summary: "string[]" } },
    },
    onItemSelectionChanged: {
      action: "selection changed",
      description:
        "Callback when a checkbox is toggled. When provided, checkboxes appear next to each item. Receives the item and checked state.",
      table: { type: { summary: "(item: TreeItem, checked: boolean) => void" } },
    },
  },
} satisfies Meta<typeof Tree>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  args: {
    renderItem: (item: TreeItem, renderNameColumn) => {
      const icon = item.data.type === "folder" ? "fa-folder" : "fa-file-o";
      return (
        <>
          <CustomDiv className="col" width="200" um="px">
            {renderNameColumn(
              <span>
                <i className={`fa ${icon}`} style={{ marginRight: "8px" }} />
                {item.data.name}
              </span>
            )}
          </CustomDiv>
          {item.data.size && (
            <CustomDiv className="col" width="100" um="px">
              {item.data.size}
            </CustomDiv>
          )}
        </>
      );
    },
  },
  parameters: {
    docs: {
      description: {
        story:
          "Interactive tree view. Click folder icons or names to expand/collapse. Click file names to see they're not expandable.",
      },
    },
  },
};

export const WithHeader: Story = {
  args: {
    header: (
      <>
        <CustomDiv className="col" width="200" um="px">
          <strong>Name</strong>
        </CustomDiv>
        <CustomDiv className="col" width="100" um="px">
          <strong>Size</strong>
        </CustomDiv>
      </>
    ),
    renderItem: (item: TreeItem, renderNameColumn) => {
      const icon = item.data.type === "folder" ? "fa-folder" : "fa-file-o";
      return (
        <>
          <CustomDiv className="col" width="200" um="px">
            {renderNameColumn(
              <span>
                <i className={`fa ${icon}`} style={{ marginRight: "8px" }} />
                {item.data.name}
              </span>
            )}
          </CustomDiv>
          {item.data.size && (
            <CustomDiv className="col" width="100" um="px">
              {item.data.size}
            </CustomDiv>
          )}
        </>
      );
    },
  },
  parameters: {
    docs: {
      description: {
        story: "Tree with a header row showing column labels.",
      },
    },
  },
};

export const InitiallyExpanded: Story = {
  args: {
    data: simpleTreeData,
    initiallyExpanded: ["folder1", "folder2", "folder3"],
    renderItem: (item: TreeItem, renderNameColumn) => {
      const icon = item.data.type === "folder" ? "fa-folder-open" : "fa-file-o";
      return (
        <CustomDiv className="col" width="300" um="px">
          {renderNameColumn(
            <span>
              <i className={`fa ${icon}`} style={{ marginRight: "8px" }} />
              {item.data.name}
            </span>
          )}
        </CustomDiv>
      );
    },
  },
  parameters: {
    docs: {
      description: {
        story: "Tree with specific nodes expanded by default using initiallyExpanded prop.",
      },
    },
  },
};

export const WithSelection: Story = {
  args: {
    data: simpleTreeData,
    initiallyExpanded: ["folder1", "folder2"],
    initiallySelected: ["file2", "folder3"],
    onItemSelectionChanged: action("selection changed"),
    renderItem: (item: TreeItem, renderNameColumn) => {
      const icon = item.data.type === "folder" ? "fa-folder" : "fa-file-o";
      return (
        <CustomDiv className="col" width="300" um="px">
          {renderNameColumn(
            <span>
              <i className={`fa ${icon}`} style={{ marginRight: "8px" }} />
              {item.data.name}
            </span>
          )}
        </CustomDiv>
      );
    },
  },
  parameters: {
    docs: {
      description: {
        story:
          "Tree with checkboxes for multi-selection. Check the Actions tab to see selection change events. Some items are pre-selected via initiallySelected.",
      },
    },
  },
};

export const OrganizationChart: Story = {
  args: {
    data: organizationTreeData,
    initiallyExpanded: ["eng", "backend"],
    header: (
      <>
        <CustomDiv className="col" width="250" um="px">
          <strong>Department</strong>
        </CustomDiv>
        <CustomDiv className="col" width="100" um="px">
          <strong>Employees</strong>
        </CustomDiv>
      </>
    ),
    renderItem: (item: TreeItem, renderNameColumn) => {
      return (
        <>
          <CustomDiv className="col" width="250" um="px">
            {renderNameColumn(
              <span>
                <i className="fa fa-sitemap" style={{ marginRight: "8px" }} />
                {item.data.name}
              </span>
            )}
          </CustomDiv>
          <CustomDiv className="col" width="100" um="px">
            {item.data.count}
          </CustomDiv>
        </>
      );
    },
  },
  parameters: {
    docs: {
      description: {
        story: "Tree used to display organizational hierarchy with employee counts.",
      },
    },
  },
};

export const EmptyTree: Story = {
  args: {
    data: { rootId: "root", items: [{ id: "root", children: [] }] },
    renderItem: (item: TreeItem, renderNameColumn) => {
      return <div>{renderNameColumn(item.id)}</div>;
    },
  },
  parameters: {
    docs: {
      description: {
        story: "Tree with no data displays 'No data' message.",
      },
    },
  },
};
