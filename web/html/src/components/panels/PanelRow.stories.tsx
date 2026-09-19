import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Panel } from "./Panel";
import { PanelRow } from "./PanelRow";

const panels = (
  <>
    <div className="col-md-6">
      <Panel headingLevel="h3" title="First panel">
        First panel content
      </Panel>
    </div>
    <div className="col-md-6">
      <Panel headingLevel="h3" title="Second panel">
        Second panel content
      </Panel>
    </div>
  </>
);

const meta = {
  title: "Components/Panels/PanelRow",
  component: PanelRow,
  parameters: {
    docs: {
      description: {
        component: "Bootstrap row wrapper used to arrange one or more Uyuni panels in a grid.",
      },
    },
  },
  args: {
    className: "",
    children: panels,
  },
  argTypes: {
    className: {
      control: "text",
      description: "Additional CSS classes appended to the Bootstrap `row` class.",
      table: { type: { summary: "string" } },
    },
    children: {
      control: false,
      description: "Panels or other grid columns rendered inside the row.",
      table: { type: { summary: "ReactNode" } },
    },
  },
} satisfies Meta<typeof PanelRow>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
