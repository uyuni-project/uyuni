import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { DeleteDialog } from "./DeleteDialog";

const meta = {
  title: "Deprecated/Dialogs/DeleteDialog",
  component: DeleteDialog,
  parameters: {
    docs: {
      description: {
        component:
          "Legacy delete confirmation dialog using Bootstrap modal. Pre-configured with 'Delete' submit button styling. For new code, prefer the newer `DangerDialog` component with controlled state.",
      },
    },
  },
  args: {
    id: "delete-dialog-story",
    title: "Delete System",
    content: "Are you sure you want to delete this system? This action cannot be undone.",
    item: { id: 123, name: "server.example.com" },
    onConfirm: action("confirmed"),
    onClosePopUp: action("closed"),
  },
  argTypes: {
    id: {
      control: "text",
      description: "HTML identifier for the Bootstrap modal dialog.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Dialog title displayed in the header.",
      table: { type: { summary: "ReactNode" } },
    },
    content: {
      control: "text",
      description: "Main confirmation message or content.",
      table: { type: { summary: "ReactNode" } },
    },
    item: {
      control: "object",
      description: "Data item being deleted, passed to the onConfirm callback.",
      table: { type: { summary: "any" } },
    },
    onConfirm: {
      action: "confirmed",
      description: "Synchronous callback invoked when the Delete button is clicked. Receives the item as parameter.",
      table: { type: { summary: "(item: any) => any" } },
    },
    onConfirmAsync: {
      control: false,
      description: "Alternative async callback that returns a Promise. Used for async delete operations.",
      table: { type: { summary: "() => Promise<any>" } },
    },
    onClosePopUp: {
      action: "closed",
      description: "Called when the dialog is closed via Cancel, close button, or after confirmation.",
      table: { type: { summary: "() => void" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes for the modal dialog.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof DeleteDialog>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: (args) => {
    return (
      <div>
        <Button
          className="btn-danger"
          text="Delete System"
          icon="fa-trash"
          handler={() => {
            jQuery(`#${args.id}`).modal("show");
          }}
        />
        <DeleteDialog {...args} />
      </div>
    );
  },
  parameters: {
    docs: {
      description: {
        story: "Click the 'Delete System' button to open the delete confirmation dialog. This uses Bootstrap modal.",
      },
    },
  },
};

export const DeleteUser: Story = {
  args: {
    id: "delete-user-dialog",
    title: "Delete User",
    content: "Deleting this user will remove all their data and access permissions. Are you sure?",
    item: { userId: 456, username: "jdoe" },
  },
  render: (args) => {
    return (
      <div>
        <Button
          className="btn-danger"
          text="Delete User"
          icon="fa-user-times"
          handler={() => {
            jQuery(`#${args.id}`).modal("show");
          }}
        />
        <DeleteDialog {...args} />
      </div>
    );
  },
  parameters: {
    docs: {
      description: {
        story: "Delete dialog configured for user deletion with appropriate messaging.",
      },
    },
  },
};

export const AsyncDelete: Story = {
  args: {
    id: "async-delete-dialog",
    title: "Delete Configuration",
    content: "This will permanently delete the configuration and all associated settings.",
    item: { configId: 789 },
    onConfirm: undefined,
    onConfirmAsync: async () => {
      action("async delete started")();
      await new Promise((resolve) => setTimeout(resolve, 1500));
      action("async delete completed")();
    },
  },
  render: (args) => {
    return (
      <div>
        <Button
          className="btn-danger"
          text="Delete Configuration"
          icon="fa-trash"
          handler={() => {
            jQuery(`#${args.id}`).modal("show");
          }}
        />
        <DeleteDialog {...args} />
      </div>
    );
  },
  parameters: {
    docs: {
      description: {
        story:
          "Delete dialog with async confirmation handler. The Delete button shows a spinner during the async operation.",
      },
    },
  },
};

export const DeleteMultiple: Story = {
  args: {
    id: "delete-multiple-dialog",
    title: "Delete Multiple Systems",
    content: (
      <div>
        <p>You are about to delete the following systems:</p>
        <ul>
          <li>server01.example.com</li>
          <li>server02.example.com</li>
          <li>server03.example.com</li>
        </ul>
        <p>
          <strong>This action cannot be undone.</strong>
        </p>
      </div>
    ),
    item: { systemIds: [1, 2, 3] },
  },
  render: (args) => {
    return (
      <div>
        <Button
          className="btn-danger"
          text="Delete 3 Systems"
          icon="fa-trash"
          handler={() => {
            jQuery(`#${args.id}`).modal("show");
          }}
        />
        <DeleteDialog {...args} />
      </div>
    );
  },
  parameters: {
    docs: {
      description: {
        story: "Delete dialog with custom content listing multiple items to be deleted.",
      },
    },
  },
};
