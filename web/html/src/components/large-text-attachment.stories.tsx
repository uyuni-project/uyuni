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
      table: { type: { summary: "string | null" } },
    },
    filename: {
      control: "text",
      description: "Suggested filename used by the download action.",
      table: { type: { summary: "string" }, defaultValue: { summary: "attachment.txt" } },
    },
    hideMessage: {
      control: "boolean",
      description: "Hides the status message displayed before the action buttons.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    presentMessage: {
      control: "text",
      description: "Status message displayed when `value` is present.",
      table: { type: { summary: "string" }, defaultValue: { summary: "Data is present." } },
    },
    absentMessage: {
      control: "text",
      description: "Status message displayed when `value` is `null`.",
      table: { type: { summary: "string" }, defaultValue: { summary: "Data is not present." } },
    },
    editable: {
      control: "boolean",
      description: "Shows add or edit controls and, when data is present, the delete control.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    downloadable: {
      control: "boolean",
      description: "Shows the download action when data is present.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "true" } },
    },
    editDialogTitle: {
      control: "text",
      description: "Heading used by the add and edit dialog.",
      table: { type: { summary: "string" }, defaultValue: { summary: "Edit" } },
    },
    editMessage: {
      control: "text",
      description: "Optional explanatory message displayed above the edit form.",
      table: { type: { summary: "string" } },
    },
    confirmDeleteMessage: {
      control: "text",
      description: "Message displayed in the delete confirmation dialog.",
      table: { type: { summary: "string" }, defaultValue: { summary: "Are you sure?" } },
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
      table: {
        type: { summary: "ButtonMode" },
        defaultValue: { summary: "ButtonMode.TextAndIcon" },
      },
    },
    disabled: {
      control: "boolean",
      description: "Disables all available attachment actions.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    onEdit: {
      control: false,
      description: "Promise-based callback invoked with the replacement text after a valid edit is submitted.",
      table: { type: { summary: "(value: string) => Promise<any>" } },
    },
    onDelete: {
      control: false,
      description: "Promise-based callback invoked after deletion is confirmed.",
      table: { type: { summary: "() => Promise<any>" } },
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
