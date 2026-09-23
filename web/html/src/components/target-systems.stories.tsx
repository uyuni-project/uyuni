import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Column } from "components/table/Column";

import { TargetSystems } from "./target-systems";

type StorySystem = {
  id: number;
  name: string;
  environment: string;
};

const systems: StorySystem[] = [
  { id: 101, name: "web-01.example.com", environment: "Production" },
  { id: 102, name: "web-02.example.com", environment: "Production" },
  { id: 201, name: "test-01.example.com", environment: "Testing" },
];

const meta = {
  title: "Components/Data Display/TargetSystems",
  component: TargetSystems,
  parameters: {
    docs: {
      description: {
        component:
          "Standard target-system summary combining a page heading and system table. Callers can append columns for workflow-specific information.",
      },
    },
  },
  args: {
    systemsData: systems,
  },
  argTypes: {
    systemsData: {
      control: false,
      description: "Target systems identified by numeric ID and display name.",
    },
    children: {
      control: false,
      description: "Optional additional table columns appended after the system-name column.",
    },
  },
  render: (args) => (
    <TargetSystems {...args}>
      <Column columnKey="environment" header="Environment" cell={(system: StorySystem) => system.environment} />
    </TargetSystems>
  ),
} satisfies Meta<typeof TargetSystems>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
