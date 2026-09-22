import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { ActionConfirm } from "./ActionConfirm";

type ActionConfirmProps = React.ComponentProps<typeof ActionConfirm>;

const meta = {
  title: "Components/Dialog/ActionConfirm",
  component: ActionConfirm,
  parameters: {
    docs: {
      description: {
        component:
          "A confirmation dialog for actions on selected items. Based on DangerDialog with additional text and an optional force checkbox. Displays selected item names and provides callbacks for confirm/cancel actions.",
      },
    },
  },
  args: {
    id: "action-confirm-dialog",
    type: "delete",
    name: "Delete",
    itemName: "item",
    icon: "fa-trash",
    canForce: false,
  },
  argTypes: {
    id: {
      control: "text",
      description: "Unique ID for the dialog element.",
      table: { type: { summary: "string" } },
    },
    type: {
      control: "text",
      description: "Action type identifier passed to onConfirm callback.",
      table: { type: { summary: "string" } },
    },
    name: {
      control: "text",
      description: "Display name of the action (e.g., 'Delete', 'Remove', 'Archive').",
      table: { type: { summary: "string" } },
    },
    itemName: {
      control: "text",
      description: "Singular name of the item type being acted upon (e.g., 'server', 'user', 'package').",
      table: { type: { summary: "string" } },
    },
    icon: {
      control: "text",
      description: "Font Awesome icon class for the action (e.g., 'fa-trash', 'fa-power-off').",
      table: { type: { summary: "string" } },
    },
    selected: {
      control: false,
      description: "Array of selected items. Each item should have a 'name' property.",
      table: { type: { summary: "{ name: string }[]" } },
    },
    canForce: {
      control: "boolean",
      description: "Whether to show a force checkbox for forceful action execution.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    forceName: {
      control: "text",
      description: "Label for the force option when canForce is true (e.g., 'Purge', 'Force delete').",
      table: { type: { summary: "string" } },
    },
    onConfirm: {
      action: "confirmed",
      description: "Callback when user confirms the action. Receives (type, selected, { force: boolean }).",
      table: { type: { summary: "(type: string, selected: any[], params: { force?: boolean }) => void" } },
    },
    onClose: {
      action: "closed",
      description: "Callback when dialog is closed (cancel or after confirm).",
      table: { type: { summary: "() => void" } },
    },
    isOpen: {
      control: "boolean",
      description: "Controls dialog visibility.",
      table: { type: { summary: "boolean" } },
    },
  },
} satisfies Meta<ActionConfirmProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const DeleteServersComponent = () => {
  const [isOpen, setIsOpen] = useState(false);
  const items = [
    { name: "web-server-01.example.com" },
    { name: "db-server-02.example.com" },
    { name: "app-server-03.example.com" },
  ];

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "20px" }}>
        <h4>Selected Servers:</h4>
        <ul>
          {items.map((item, idx) => (
            <li key={idx}>{item.name}</li>
          ))}
        </ul>
      </div>

      <Button className="btn-danger" text="Delete Selected Servers" handler={() => setIsOpen(true)} />

      <ActionConfirm
        id="delete-servers-confirm"
        type="delete"
        name="Delete"
        itemName="server"
        icon="fa-trash"
        selected={items}
        onConfirm={(type, selected, params) => {
          const names = selected.map((obj) => obj.name).join(", ");
          action("confirmed")({ type, names, force: params.force || false });
          setIsOpen(false);
        }}
        canForce={false}
        isOpen={isOpen}
        onClose={() => {
          action("closed")();
          setIsOpen(false);
        }}
      />
    </div>
  );
};

export const Playground: Story = {
  render: () => <DeleteServersComponent />,
  parameters: {
    docs: {
      description: {
        story:
          "Interactive action confirmation dialog. Click the button to see the confirmation dialog for deleting servers.",
      },
    },
  },
};

const WithForceComponent = () => {
  const [isOpen, setIsOpen] = useState(false);
  const items = [{ name: "legacy-system-01" }, { name: "legacy-system-02" }];

  return (
    <div style={{ padding: "20px" }}>
      <div
        style={{
          padding: "15px",
          background: "#fff3cd",
          border: "1px solid #ffc107",
          borderRadius: "4px",
          marginBottom: "20px",
        }}
      >
        <strong>Note:</strong> This dialog includes a force option that allows bypassing safety checks.
      </div>

      <Button className="btn-danger" text="Remove Legacy Systems" handler={() => setIsOpen(true)} />

      <ActionConfirm
        id="remove-systems-confirm"
        type="remove"
        name="Remove"
        itemName="system"
        icon="fa-times-circle"
        selected={items}
        onConfirm={(type, selected, params) => {
          const names = selected.map((obj) => obj.name).join(", ");
          action("confirmed")({ type, names, force: params.force || false });
          setIsOpen(false);
        }}
        canForce={true}
        forceName="Purge"
        isOpen={isOpen}
        onClose={() => {
          action("closed")();
          setIsOpen(false);
        }}
      />
    </div>
  );
};

export const WithForce: Story = {
  render: () => <WithForceComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Action confirmation with force checkbox. The force option allows users to bypass safety checks or perform a more aggressive action (e.g., 'Purge' instead of 'Remove').",
      },
    },
  },
};

const PowerOffComponent = () => {
  const [isOpen, setIsOpen] = useState(false);
  const items = [{ name: "production-app-01" }, { name: "production-app-02" }, { name: "production-app-03" }];

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-warning" text="Power Off Production Servers" handler={() => setIsOpen(true)} />

      <ActionConfirm
        id="poweroff-confirm"
        type="poweroff"
        name="Power Off"
        itemName="server"
        icon="fa-power-off"
        selected={items}
        onConfirm={(type, selected, params) => {
          const names = selected.map((obj) => obj.name).join(", ");
          action("confirmed")({ type, names, force: params.force || false });
          setIsOpen(false);
        }}
        canForce={false}
        isOpen={isOpen}
        onClose={() => {
          action("closed")();
          setIsOpen(false);
        }}
      />
    </div>
  );
};

export const PowerOff: Story = {
  render: () => <PowerOffComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Action confirmation for powering off servers. Uses a warning button style and power-off icon.",
      },
    },
  },
};

const SingleItemComponent = () => {
  const [isOpen, setIsOpen] = useState(false);
  const items = [{ name: "critical-database-server" }];

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-danger" text="Delete Critical Server" handler={() => setIsOpen(true)} />

      <ActionConfirm
        id="delete-single-confirm"
        type="delete"
        name="Delete"
        itemName="server"
        icon="fa-trash"
        selected={items}
        onConfirm={(type, selected, params) => {
          action("confirmed")({ type, item: selected[0].name, force: params.force || false });
          setIsOpen(false);
        }}
        canForce={true}
        forceName="Force delete"
        isOpen={isOpen}
        onClose={() => {
          action("closed")();
          setIsOpen(false);
        }}
      />
    </div>
  );
};

export const SingleItem: Story = {
  render: () => <SingleItemComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Action confirmation for a single item. The dialog text adapts to singular form.",
      },
    },
  },
};

const ManyItemsComponent = () => {
  const [isOpen, setIsOpen] = useState(false);
  const items = Array.from({ length: 10 }, (_, i) => ({ name: `server-${String(i + 1).padStart(2, "0")}` }));

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "20px" }}>
        <strong>{items.length} servers selected</strong>
      </div>

      <Button className="btn-danger" text="Delete All Selected" handler={() => setIsOpen(true)} />

      <ActionConfirm
        id="delete-many-confirm"
        type="delete"
        name="Delete"
        itemName="server"
        icon="fa-trash"
        selected={items}
        onConfirm={(type, selected, params) => {
          action("confirmed")({ type, count: selected.length, force: params.force || false });
          setIsOpen(false);
        }}
        canForce={false}
        isOpen={isOpen}
        onClose={() => {
          action("closed")();
          setIsOpen(false);
        }}
      />
    </div>
  );
};

export const ManyItems: Story = {
  render: () => <ManyItemsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Action confirmation for many items. The dialog lists all selected items for review.",
      },
    },
  },
};
