import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { DangerDialog } from "./DangerDialog";

type DangerDialogProps = React.ComponentProps<typeof DangerDialog>;

const StatefulDangerDialog = (props: DangerDialogProps) => {
  const [isOpen, setIsOpen] = useState(props.isOpen);

  useEffect(() => setIsOpen(props.isOpen), [props.isOpen]);

  return (
    <>
      <Button className="btn-danger" text="Open dangerous action" handler={() => setIsOpen(true)} />
      <DangerDialog
        {...props}
        isOpen={isOpen}
        onClose={() => {
          setIsOpen(false);
          props.onClose?.();
        }}
      />
    </>
  );
};

const meta = {
  title: "Components/Dialogs/DangerDialog",
  component: DangerDialog,
  parameters: {
    docs: {
      description: {
        component:
          "Confirmation dialog for destructive or high-impact operations. It adds confirm and cancel actions to the base `Dialog`.",
      },
    },
  },
  args: {
    isOpen: false,
    id: "storybook-danger-dialog",
    title: "Delete system",
    content: "This operation cannot be undone. Do you want to continue?",
    submitText: "Delete",
    submitIcon: "fa-trash",
    btnClass: "btn-danger",
    item: { id: 42, name: "example.example.org" },
    onConfirm: action("confirmed"),
    onClose: action("closed"),
    closableModal: true,
  },
  argTypes: {
    submitText: {
      control: "text",
      description: "Text displayed on the confirmation button.",
      table: { type: { summary: "string" } },
    },
    submitIcon: {
      control: "text",
      description: "Font Awesome class displayed on the confirmation button.",
      table: { type: { summary: "string" } },
    },
    btnClass: {
      control: "select",
      options: ["btn-danger", "btn-primary", "btn-default"],
      description: "Uyuni variant applied to the confirmation button.",
      table: { type: { summary: "string" }, defaultValue: { summary: "btn-danger" } },
    },
    item: {
      control: "object",
      description: "Caller-owned value passed to the synchronous confirmation callback.",
      table: { type: { summary: "any" } },
    },
    onConfirm: {
      action: "confirmed",
      description: "Synchronous confirmation callback receiving `item`.",
      table: { type: { summary: "(item: any) => any" } },
    },
    onConfirmAsync: {
      control: false,
      description: "Alternative promise-based confirmation callback.",
      table: { type: { summary: "(item: any) => Promise<any>" } },
    },
    isOpen: {
      control: "boolean",
      description: "Whether the dialog is currently visible.",
      table: { type: { summary: "boolean" } },
    },
    onClose: {
      action: "closed",
      description: "Called when confirmation, cancellation, or the dialog close control requests closure.",
      table: { type: { summary: "() => void" } },
    },
    id: {
      control: "text",
      description: "HTML identifier assigned to the dialog.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the dialog element.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Content displayed in the dialog heading.",
      table: { type: { summary: "ReactNode" } },
    },
    content: {
      control: "text",
      description: "Main confirmation message or content.",
      table: { type: { summary: "ReactNode" } },
    },
    hideHeader: {
      control: "boolean",
      description: "Hides the dialog header.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    closableModal: {
      control: "boolean",
      description: "Allows closing through the close button, Escape key, or overlay.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "true" } },
    },
  },
  render: (args) => <StatefulDangerDialog {...args} />,
} satisfies Meta<typeof DangerDialog>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "The dialog starts closed so it does not cover the documentation. Use the trigger button or `isOpen` control to open it.",
      },
    },
  },
};

export const PrimaryConfirmation: Story = {
  args: {
    title: "Apply configuration",
    content: "Apply the selected configuration to this system?",
    submitText: "Apply",
    submitIcon: "fa-check",
    btnClass: "btn-primary",
  },
};
