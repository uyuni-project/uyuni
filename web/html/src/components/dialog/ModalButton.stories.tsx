import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Dialog as LegacyDialog } from "./LegacyDialog";
import { ModalButton } from "./ModalButton";

type ModalButtonProps = React.ComponentProps<typeof ModalButton>;

const meta = {
  title: "Components/Dialogs/ModalButton",
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
    target: "modal-button-example",
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

const ModalButtonWithDialog = (args: ModalButtonProps) => (
  <div style={{ padding: "20px" }}>
    <ModalButton {...args} />
    <LegacyDialog
      id={args.target ?? "modal-button-example"}
      title="Example Modal"
      content={
        <div style={{ padding: "20px" }}>
          <p>This dialog is opened through the ModalButton target prop.</p>
          <p>The target must match the ID of a legacy Bootstrap dialog.</p>
        </div>
      }
      buttons={
        <button
          className="btn btn-default"
          onClick={() => jQuery("#" + (args.target ?? "modal-button-example")).modal("hide")}
        >
          {t("Close")}
        </button>
      }
    />
  </div>
);

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
  return (
    <div style={{ padding: "20px", display: "flex", gap: "10px" }}>
      <ModalButton text="Modal A" className="btn-default" target="modal-a" onClick={action("Modal A clicked")} />
      <ModalButton
        text="Modal B"
        icon="fa-info"
        className="btn-info"
        target="modal-b"
        onClick={action("Modal B clicked")}
      />
      <ModalButton
        text="Modal C"
        icon="fa-cog"
        className="btn-primary"
        target="modal-c"
        onClick={action("Modal C clicked")}
      />

      <LegacyDialog id="modal-a" title="Modal A" content={<div>Content for Modal A</div>} />
      <LegacyDialog id="modal-b" title="Modal B" content={<div>Content for Modal B</div>} />
      <LegacyDialog id="modal-c" title="Modal C" content={<div>Content for Modal C</div>} />
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
