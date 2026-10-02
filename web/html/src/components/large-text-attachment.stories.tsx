import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { ButtonMode, LargeTextAttachment } from "./large-text-attachment";

const edited = action("edited");
const deleted = action("deleted");

const meta = {
  title: "Components/Inputs/LargeTextAttachment",
  component: LargeTextAttachment,
  parameters: {
    docs: {
      description: {
        component:
          "Displays whether a large text value is present and provides configurable download, edit, and delete actions. Editing supports both file upload and pasted text.",
      },
    },
  },
  args: {
    value: "server:\n  port: 443\n  protocol: https\n",
    filename: "server-configuration.yaml",
    hideMessage: false,
    presentMessage: "Configuration data is available.",
    absentMessage: "No configuration data has been provided.",
    editable: false,
    downloadable: true,
    editDialogTitle: "Edit configuration data",
    editMessage: "Upload a replacement file or paste the new configuration.",
    confirmDeleteMessage: "Delete the stored configuration data?",
    buttonMode: ButtonMode.TextAndIcon,
    disabled: false,
    onEdit: async (value) => {
      edited(value);
    },
    onDelete: async () => {
      deleted();
    },
  },
  argTypes: {
    value: {
      control: "text",
      description: "Stored text value. `null` represents an attachment that has not been provided.",
    },
    filename: {
      control: "text",
      description: "Suggested filename used by the download action.",
    },
    hideMessage: {
      control: "boolean",
      description: "Hides the status message displayed before the action buttons.",
    },
    presentMessage: {
      control: "text",
      description: "Status message displayed when `value` is present.",
    },
    absentMessage: {
      control: "text",
      description: "Status message displayed when `value` is `null`.",
    },
    editable: {
      control: "boolean",
      description: "Shows add or edit controls and, when data is present, the delete control.",
    },
    downloadable: {
      control: "boolean",
      description: "Shows the download action when data is present.",
    },
    editDialogTitle: {
      control: "text",
      description: "Heading used by the add and edit dialog.",
    },
    editMessage: {
      control: "text",
      description: "Optional explanatory message displayed above the edit form.",
    },
    confirmDeleteMessage: {
      control: "text",
      description: "Message displayed in the delete confirmation dialog.",
    },
    buttonMode: {
      control: "select",
      options: [ButtonMode.TextAndIcon, ButtonMode.Text, ButtonMode.Icon],
      labels: {
        [ButtonMode.TextAndIcon]: "Text and icon",
        [ButtonMode.Text]: "Text",
        [ButtonMode.Icon]: "Icon",
      },
      description: "Determines whether action buttons display text, icons, or both.",
    },
    disabled: {
      control: "boolean",
      description: "Disables all available attachment actions.",
    },
    onEdit: {
      control: false,
      description: "Promise-based callback invoked with the replacement text after a valid edit is submitted.",
    },
    onDelete: {
      control: false,
      description: "Promise-based callback invoked after deletion is confirmed.",
    },
  },
} satisfies Meta<typeof LargeTextAttachment>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const NoData: Story = {
  args: {
    value: null,
    editable: true,
  },
  parameters: {
    docs: { description: { story: "Without stored data, the editable component offers an add action." } },
  },
};

export const Editable: Story = {
  args: {
    editable: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Stored data can be downloaded, replaced, or deleted. The dialogs remain closed until requested.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    editable: true,
    disabled: true,
  },
};

export const IconButtons: Story = {
  args: {
    editable: true,
    buttonMode: ButtonMode.Icon,
  },
};
