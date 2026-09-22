import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Dialog } from "./Dialog";
import { ModalButton } from "./ModalButton";

type ModalButtonProps = React.ComponentProps<typeof ModalButton>;

const meta = {
  title: "Components/Dialog/ModalButton",
  component: ModalButton,
  parameters: {
    docs: {
      description: {
        component:
          "Button that launches a modal dialog. Extends the standard Button component with modal triggering functionality. Use the `target` prop to specify the modal ID, and `onClick` callback for custom actions before showing the dialog.",
      },
    },
  },
  args: {
    text: "Open Modal",
    className: "btn-default",
    disabled: false,
  },
  argTypes: {
    text: {
      control: "text",
      description: "Text label displayed on the button.",
      table: { type: { summary: "string" } },
    },
    icon: {
      control: "text",
      description: "Font Awesome icon class (e.g., 'fa-edit', 'fa-plus').",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "CSS classes for the button. Use Bootstrap button classes (btn-default, btn-primary, etc.).",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "HTML title attribute for tooltip on hover.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Disables the button and prevents modal from opening.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    target: {
      control: "text",
      description: "ID of the modal dialog to show when button is clicked.",
      table: { type: { summary: "string" } },
    },
    onClick: {
      action: "clicked",
      description: "Callback invoked before showing the modal. Receives the item prop as parameter.",
      table: { type: { summary: "(item?: any) => void" } },
    },
    item: {
      control: false,
      description: "Optional data object passed to onClick callback.",
      table: { type: { summary: "any" } },
    },
  },
} satisfies Meta<typeof ModalButton>;

export default meta;

type Story = StoryObj<typeof meta>;

const ModalButtonWithDialog = (args: ModalButtonProps) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div style={{ padding: "20px" }}>
      <ModalButton
        {...args}
        onClick={(item) => {
          action("button clicked")(item);
          setIsOpen(true);
        }}
      />
      <Dialog
        id="example-modal"
        title="Example Modal"
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        content={
          <div style={{ padding: "20px" }}>
            <p>This is the modal content triggered by the ModalButton.</p>
            <p>You can put any content here: forms, tables, images, etc.</p>
          </div>
        }
        footer={
          <div className="col-lg-12">
            <div className="pull-right btn-group">
              <button className="btn btn-default" onClick={() => setIsOpen(false)}>
                {t("Close")}
              </button>
            </div>
          </div>
        }
      />
    </div>
  );
};

export const Playground: Story = {
  render: (args) => <ModalButtonWithDialog {...args} />,
  parameters: {
    docs: {
      description: {
        story:
          "Interactive ModalButton. Click the button to see the modal dialog appear. The button can be customized with different text, icons, and styles.",
      },
    },
  },
};

export const WithIcon: Story = {
  render: (args) => <ModalButtonWithDialog {...args} />,
  args: {
    text: "Edit Settings",
    icon: "fa-edit",
    className: "btn-primary",
  },
  parameters: {
    docs: {
      description: {
        story: "ModalButton with an icon. Icons appear before the text label.",
      },
    },
  },
};

export const Disabled: Story = {
  render: (args) => <ModalButtonWithDialog {...args} />,
  args: {
    text: "Cannot Open",
    disabled: true,
    title: "This action is not available",
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled ModalButton. The modal will not open when clicked.",
      },
    },
  },
};

export const DangerAction: Story = {
  render: (args) => <ModalButtonWithDialog {...args} />,
  args: {
    text: "Delete",
    icon: "fa-trash",
    className: "btn-danger",
    title: "Delete this item",
  },
  parameters: {
    docs: {
      description: {
        story: "ModalButton for dangerous actions. Use btn-danger class for destructive operations.",
      },
    },
  },
};

const MultipleButtonsComponent = () => {
  const [openModal, setOpenModal] = useState<string | null>(null);

  return (
    <div style={{ padding: "20px", display: "flex", gap: "10px" }}>
      <ModalButton
        text="Modal A"
        className="btn-default"
        onClick={() => {
          action("Modal A clicked")();
          setOpenModal("modal-a");
        }}
      />
      <ModalButton
        text="Modal B"
        icon="fa-info"
        className="btn-info"
        onClick={() => {
          action("Modal B clicked")();
          setOpenModal("modal-b");
        }}
      />
      <ModalButton
        text="Modal C"
        icon="fa-cog"
        className="btn-primary"
        onClick={() => {
          action("Modal C clicked")();
          setOpenModal("modal-c");
        }}
      />

      <Dialog
        id="modal-a"
        title="Modal A"
        isOpen={openModal === "modal-a"}
        onClose={() => setOpenModal(null)}
        content={<div style={{ padding: "20px" }}>Content for Modal A</div>}
      />
      <Dialog
        id="modal-b"
        title="Modal B"
        isOpen={openModal === "modal-b"}
        onClose={() => setOpenModal(null)}
        content={<div style={{ padding: "20px" }}>Content for Modal B</div>}
      />
      <Dialog
        id="modal-c"
        title="Modal C"
        isOpen={openModal === "modal-c"}
        onClose={() => setOpenModal(null)}
        content={<div style={{ padding: "20px" }}>Content for Modal C</div>}
      />
    </div>
  );
};

export const MultipleButtons: Story = {
  render: () => <MultipleButtonsComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Multiple ModalButtons each triggering different modals.",
      },
    },
  },
};
