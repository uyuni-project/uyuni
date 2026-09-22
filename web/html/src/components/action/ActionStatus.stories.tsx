import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { ActionStatus } from "./ActionStatus";

const meta = {
  title: "Components/Data Display/ActionStatus",
  component: ActionStatus,
  parameters: {
    docs: {
      description: {
        component:
          "Displays an action status as a clickable icon linking to the action details page. Icons and colors change based on the status: Queued (clock, blue), Failed (X, red), Completed (checkmark, green), or Picked Up (exchange, blue). Automatically includes tooltip support.",
      },
    },
  },
  args: {
    serverId: "1000010000",
    actionId: "123",
    status: "Completed",
  },
  argTypes: {
    serverId: {
      control: "text",
      description: "ID of the server the action is running on. Used to construct the link URL.",
      table: { type: { summary: "string" } },
    },
    actionId: {
      control: "text",
      description: "ID of the action to display. Used to construct the link URL.",
      table: { type: { summary: "string" } },
    },
    status: {
      control: "select",
      options: ["Queued", "Failed", "Completed", "Picked Up"],
      description:
        "Status name of the action. Determines the icon and color: Queued (clock), Failed (X), Completed (checkmark), Picked Up (exchange).",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof ActionStatus>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive action status display. Hover to see the status tooltip. Click to navigate (in real use).",
      },
    },
  },
};

export const Queued: Story = {
  args: {
    status: "Queued",
  },
  parameters: {
    docs: {
      description: {
        story: "Queued action - displays a blue clock icon indicating the action is waiting to execute.",
      },
    },
  },
};

export const Failed: Story = {
  args: {
    status: "Failed",
  },
  parameters: {
    docs: {
      description: {
        story: "Failed action - displays a red X icon indicating the action execution failed.",
      },
    },
  },
};

export const Completed: Story = {
  args: {
    status: "Completed",
  },
  parameters: {
    docs: {
      description: {
        story: "Completed action - displays a green checkmark icon indicating successful execution.",
      },
    },
  },
};

export const PickedUp: Story = {
  args: {
    status: "Picked Up",
  },
  parameters: {
    docs: {
      description: {
        story:
          "Picked Up action - displays a blue exchange icon indicating the action has been picked up for execution.",
      },
    },
  },
};

export const AllStatuses: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <ActionStatus serverId="1000010000" actionId="100" status="Queued" />
          <span>Queued</span>
        </div>
      </StoryRow>
      <StoryRow>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <ActionStatus serverId="1000010000" actionId="101" status="Picked Up" />
          <span>Picked Up</span>
        </div>
      </StoryRow>
      <StoryRow>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <ActionStatus serverId="1000010000" actionId="102" status="Completed" />
          <span>Completed</span>
        </div>
      </StoryRow>
      <StoryRow>
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <ActionStatus serverId="1000010000" actionId="103" status="Failed" />
          <span>Failed</span>
        </div>
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "All four action statuses displayed for comparison.",
      },
    },
  },
};

export const InTable: Story = {
  render: () => (
    <div style={{ padding: "20px" }}>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Action</th>
            <th>Description</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Apply updates</td>
            <td>Install security patches</td>
            <td>
              <ActionStatus serverId="1000010000" actionId="200" status="Completed" />
            </td>
          </tr>
          <tr>
            <td>Reboot system</td>
            <td>Restart after kernel update</td>
            <td>
              <ActionStatus serverId="1000010000" actionId="201" status="Queued" />
            </td>
          </tr>
          <tr>
            <td>Deploy configuration</td>
            <td>Update Apache configuration</td>
            <td>
              <ActionStatus serverId="1000010000" actionId="202" status="Failed" />
            </td>
          </tr>
          <tr>
            <td>Run remote command</td>
            <td>Execute diagnostic script</td>
            <td>
              <ActionStatus serverId="1000010000" actionId="203" status="Picked Up" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Common usage in a table showing action statuses alongside action details.",
      },
    },
  },
};
