import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { DangerDialog } from "./LegacyDangerDialog";

type LegacyDangerDialogProps = React.ComponentProps<typeof DangerDialog>;

const meta = {
  title: "Components/DEPRECATED/LegacyDangerDialog",
  component: DangerDialog,
  parameters: {
    docs: {
      description: {
        component: `
**⚠️ DEPRECATED - Do not use in new code**

This component is deprecated. Use \`import { DangerDialog } from "components/dialog"\` instead.

This component uses jQuery Bootstrap modal and will be removed in a future release.
The new DangerDialog component uses React state management with better performance and accessibility.
        `,
      },
    },
  },
  args: {
    id: "danger-dialog",
    title: "Confirm Deletion",
    submitText: "Delete",
    submitIcon: "fa-trash",
    closableModal: true,
  },
  argTypes: {
    id: {
      control: "text",
      description: "Unique ID for the dialog.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Dialog title displayed in the header.",
      table: { type: { summary: "ReactNode" } },
    },
    content: {
      control: false,
      description: "Content to display in the dialog body.",
      table: { type: { summary: "ReactNode" } },
    },
    submitText: {
      control: "text",
      description: "Text for the danger/submit button.",
      table: { type: { summary: "string" } },
    },
    submitIcon: {
      control: "text",
      description: "Icon for the danger/submit button (Font Awesome class).",
      table: { type: { summary: "string" } },
    },
    btnClass: {
      control: "text",
      description: "CSS class for the submit button.",
      table: { type: { summary: "string" }, defaultValue: { summary: "btn-danger" } },
    },
    onConfirm: {
      action: "confirmed",
      description: "Callback when submit button is clicked (sync).",
      table: { type: { summary: "(item?: any) => void" } },
    },
    onConfirmAsync: {
      action: "confirmed async",
      description: "Callback when submit button is clicked (async).",
      table: { type: { summary: "() => Promise<any>" } },
    },
    item: {
      control: false,
      description: "Data item to pass to onConfirm callback.",
      table: { type: { summary: "any" } },
    },
  },
} satisfies Meta<LegacyDangerDialogProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const DangerDialogComponent = (args: Partial<LegacyDangerDialogProps>) => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => {
    setIsOpen(true);
    setTimeout(() => jQuery("#" + (args.id || "danger-dialog")).modal("show"), 0);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-danger" text="Delete Item" icon="fa-trash" handler={openModal} />

      {isOpen && (
        <DangerDialog
          {...args}
          id={args.id || "danger-dialog"}
          content={
            <div style={{ padding: "20px" }}>
              <p>
                <strong>Warning:</strong> This action cannot be undone.
              </p>
              <p>Are you sure you want to delete this item?</p>
            </div>
          }
          onConfirm={() => {
            action("confirmed")();
            setIsOpen(false);
          }}
          onClosePopUp={() => {
            action("dialog closed")();
            setIsOpen(false);
          }}
        />
      )}
    </div>
  );
};

export const Playground: Story = {
  render: (args) => <DangerDialogComponent {...args} />,
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Legacy danger dialog for destructive actions. Use DangerDialog component for new code.",
      },
    },
  },
};

const DeleteUserComponent = () => {
  const [isOpen, setIsOpen] = useState(false);
  const user = { id: 123, name: "John Doe" };

  const openModal = () => {
    setIsOpen(true);
    setTimeout(() => jQuery("#delete-user-dialog").modal("show"), 0);
  };

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ marginBottom: "10px", padding: "10px", background: "#f5f5f5", borderRadius: "4px" }}>
        <strong>User:</strong> {user.name} (ID: {user.id})
      </div>
      <Button className="btn-danger" text="Delete User" icon="fa-trash" handler={openModal} />

      {isOpen && (
        <DangerDialog
          id="delete-user-dialog"
          title="Delete User"
          submitText="Delete"
          submitIcon="fa-trash"
          content={
            <div style={{ padding: "20px" }}>
              <p>
                Are you sure you want to delete user <strong>{user.name}</strong>?
              </p>
              <p>This will permanently remove the user and all associated data.</p>
            </div>
          }
          item={user}
          onConfirm={(item) => {
            action("user deleted")(item);
            setIsOpen(false);
          }}
          onClosePopUp={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export const DeleteUser: Story = {
  render: () => <DeleteUserComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Danger dialog for deleting a user with item data passed to callback.",
      },
    },
  },
};

const AsyncActionComponent = () => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => {
    setIsOpen(true);
    setTimeout(() => jQuery("#async-dialog").modal("show"), 0);
  };

  const handleAsyncDelete = async () => {
    action("starting async delete")();
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 2000));
    action("async delete completed")();
  };

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-danger" text="Delete with Async" icon="fa-trash" handler={openModal} />

      {isOpen && (
        <DangerDialog
          id="async-dialog"
          title="Async Deletion"
          submitText="Delete"
          submitIcon="fa-trash"
          content={
            <div style={{ padding: "20px" }}>
              <p>This will trigger an async operation (simulated 2 second delay).</p>
              <p>The button will show loading state during the operation.</p>
            </div>
          }
          onConfirmAsync={async () => {
            const result = await handleAsyncDelete();
            setIsOpen(false);
            return result;
          }}
          onClosePopUp={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export const AsyncAction: Story = {
  render: () => <AsyncActionComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Danger dialog with async confirmation using AsyncButton.",
      },
    },
  },
};

const CustomButtonClassComponent = () => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => {
    setIsOpen(true);
    setTimeout(() => jQuery("#custom-dialog").modal("show"), 0);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-warning" text="Archive Item" icon="fa-archive" handler={openModal} />

      {isOpen && (
        <DangerDialog
          id="custom-dialog"
          title="Archive Item"
          submitText="Archive"
          submitIcon="fa-archive"
          btnClass="btn-warning"
          content={
            <div style={{ padding: "20px" }}>
              <p>This action will archive the item.</p>
              <p>You can restore it later from the archive.</p>
            </div>
          }
          onConfirm={() => {
            action("item archived")();
            setIsOpen(false);
          }}
          onClosePopUp={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export const CustomButtonClass: Story = {
  render: () => <CustomButtonClassComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Danger dialog with custom button class (warning instead of danger).",
      },
    },
  },
};
