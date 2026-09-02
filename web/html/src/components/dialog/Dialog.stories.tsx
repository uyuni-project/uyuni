import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { Dialog } from "./Dialog";

type DialogProps = React.ComponentProps<typeof Dialog>;

const StatefulDialog = (props: DialogProps) => {
  const [isOpen, setIsOpen] = useState(props.isOpen);

  useEffect(() => setIsOpen(props.isOpen), [props.isOpen]);

  return (
    <>
      <Button className="btn-primary" text="Open dialog" handler={() => setIsOpen(true)} />
      <Dialog
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
  title: "Components/Dialogs/Dialog",
  component: Dialog,
  parameters: {
    docs: {
      description: {
        component:
          "Accessible modal dialog with optional header, title, footer, and close behavior. The parent controls whether it is open.",
      },
    },
  },
  args: {
    isOpen: false,
    onClose: action("closed"),
    id: "storybook-dialog",
    title: "Confirm changes",
    content: "Review the changes before continuing.",
    footer: "Dialog footer",
    hideHeader: false,
    closableModal: true,
    className: "",
  },
  argTypes: {
    isOpen: {
      control: "boolean",
      description: "Whether the modal is currently visible.",
      table: { type: { summary: "boolean" } },
    },
    onClose: {
      action: "closed",
      description: "Called when the close button, Escape key, or overlay requests that the dialog close.",
      table: { type: { summary: "() => void" } },
    },
    id: {
      control: "text",
      description: "HTML identifier assigned to the modal.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the modal dialog element.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Content displayed in the dialog heading.",
      table: { type: { summary: "ReactNode" } },
    },
    content: {
      control: "text",
      description: "Main content rendered in the dialog body.",
      table: { type: { summary: "ReactNode" } },
    },
    footer: {
      control: "text",
      description: "Optional content rendered in the dialog footer.",
      table: { type: { summary: "ReactNode" } },
    },
    hideHeader: {
      control: "boolean",
      description: "Hides the complete header, including its title and close button.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    closableModal: {
      control: "boolean",
      description: "Allows closing through the close button, Escape key, or overlay.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "true" } },
    },
  },
  render: (args) => <StatefulDialog {...args} />,
} satisfies Meta<typeof Dialog>;

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

export const NotClosable: Story = {
  args: {
    title: "Operation in progress",
    content: "This dialog remains open until the operation completes.",
    closableModal: false,
  },
};

export const WithoutHeader: Story = {
  args: {
    hideHeader: true,
    content: "A compact dialog without a header.",
  },
};
