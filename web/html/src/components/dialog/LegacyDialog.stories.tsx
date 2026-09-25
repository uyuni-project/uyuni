import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { Dialog } from "./LegacyDialog";

type LegacyDialogProps = React.ComponentProps<typeof Dialog>;

const meta = {
  title: "Deprecated/Dialogs/LegacyDialog",
  component: Dialog,
  parameters: {
    docs: {
      description: {
        component: `
**⚠️ DEPRECATED - Do not use in new code**

This component is deprecated. Use \`import { Dialog } from "components/dialog"\` instead.

This component uses jQuery Bootstrap modal and will be removed in a future release.
The new Dialog component uses React state management with better performance and accessibility.
        `,
      },
    },
  },
  args: {
    id: "legacy-dialog",
    title: "Legacy Dialog",
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
    buttons: {
      control: false,
      description: "Footer buttons for the dialog.",
      table: { type: { summary: "ReactNode" } },
    },
    closableModal: {
      control: "boolean",
      description: "Whether the dialog can be closed by clicking outside or pressing ESC.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "true" } },
    },
    autoFocus: {
      control: "boolean",
      description: "Automatically focus the first input when dialog opens.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "true" } },
    },
  },
} satisfies Meta<LegacyDialogProps>;

export default meta;

type Story = StoryObj<typeof meta>;

const LegacyDialogComponent = (args: Partial<LegacyDialogProps>) => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => {
    setIsOpen(true);
    // Use jQuery to show modal (legacy behavior)
    setTimeout(() => {
      jQuery("#" + args.id).modal("show");
    }, 0);
  };

  const closeModal = () => {
    jQuery("#" + args.id).modal("hide");
    setIsOpen(false);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-primary" text="Open Legacy Dialog" handler={openModal} />

      {isOpen && (
        <Dialog
          {...args}
          id={args.id || "legacy-dialog"}
          content={
            <div style={{ padding: "20px" }}>
              <p>This is a legacy dialog using jQuery Bootstrap modals.</p>
              <p>Use the new Dialog component for better React integration.</p>
            </div>
          }
          buttons={
            <div>
              <Button className="btn-default" text="Cancel" handler={closeModal} />
              <Button
                className="btn-primary"
                text="OK"
                handler={() => {
                  action("OK clicked")();
                  closeModal();
                }}
              />
            </div>
          }
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
  render: (args) => <LegacyDialogComponent {...args} />,
  parameters: {
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Legacy dialog using jQuery Bootstrap modal. Use Dialog component for new code.",
      },
    },
  },
};

const WithFormComponent = () => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => {
    setIsOpen(true);
    setTimeout(() => jQuery("#form-dialog").modal("show"), 0);
  };

  const closeModal = () => {
    jQuery("#form-dialog").modal("hide");
    setIsOpen(false);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-primary" text="Open Form Dialog" handler={openModal} />

      {isOpen && (
        <Dialog
          id="form-dialog"
          title="Edit User"
          content={
            <div style={{ padding: "20px" }}>
              <div className="form-group">
                <label htmlFor="username-input">Username:</label>
                <input id="username-input" type="text" className="form-control" defaultValue="john.doe" />
              </div>
              <div className="form-group">
                <label htmlFor="email-input">Email:</label>
                <input id="email-input" type="email" className="form-control" defaultValue="john@example.com" />
              </div>
            </div>
          }
          buttons={
            <div>
              <Button className="btn-default" text="Cancel" handler={closeModal} />
              <Button
                className="btn-primary"
                text="Save"
                handler={() => {
                  action("form saved")();
                  closeModal();
                }}
              />
            </div>
          }
          autoFocus
        />
      )}
    </div>
  );
};

export const WithForm: Story = {
  render: () => <WithFormComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Legacy dialog with form inputs. First input is auto-focused.",
      },
    },
  },
};

const NonClosableComponent = () => {
  const [isOpen, setIsOpen] = useState(false);

  const openModal = () => {
    setIsOpen(true);
    setTimeout(() => jQuery("#nonclosable-dialog").modal("show"), 0);
  };

  const closeModal = () => {
    jQuery("#nonclosable-dialog").modal("hide");
    setIsOpen(false);
  };

  return (
    <div style={{ padding: "20px" }}>
      <Button className="btn-primary" text="Open Non-Closable Dialog" handler={openModal} />

      {isOpen && (
        <Dialog
          id="nonclosable-dialog"
          title="Important Action"
          closableModal={false}
          content={
            <div style={{ padding: "20px" }}>
              <p>This dialog cannot be closed by clicking outside or pressing ESC.</p>
              <p>You must click one of the buttons below.</p>
            </div>
          }
          buttons={
            <div>
              <Button className="btn-default" text="Cancel" handler={closeModal} />
              <Button
                className="btn-primary"
                text="Proceed"
                handler={() => {
                  action("proceed clicked")();
                  closeModal();
                }}
              />
            </div>
          }
        />
      )}
    </div>
  );
};

export const NonClosable: Story = {
  render: () => <NonClosableComponent />,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "⚠️ DEPRECATED - Non-closable dialog. Must use buttons to close.",
      },
    },
  },
};
