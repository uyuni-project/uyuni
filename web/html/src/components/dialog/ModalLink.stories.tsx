import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Dialog } from "./LegacyDialog";
import { ModalLink } from "./ModalLink";

const meta = {
  title: "Components/Dialogs/ModalLink",
  component: ModalLink,
  parameters: {
    docs: {
      description: {
        component:
          "**DEPRECATED:** Link-styled button that opens a Bootstrap modal dialog. For new code, use `ModalButton` instead. This component is provided for legacy compatibility only.",
      },
    },
  },
  args: {
    target: "example-modal",
    text: "Open Dialog",
    icon: "fa-external-link",
    disabled: false,
  },
  argTypes: {
    target: {
      control: "text",
      description: "ID of the modal dialog to open.",
      table: { type: { summary: "string" } },
    },
    text: {
      control: "text",
      description: "Link text to display.",
      table: { type: { summary: "ReactNode" } },
    },
    icon: {
      control: "text",
      description: "Font Awesome icon class to display before the text.",
      table: { type: { summary: "string" } },
    },
    id: {
      control: "text",
      description: "HTML identifier for the link element.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "text",
      description: "Additional CSS classes appended to the button. Base class is 'btn-tertiary'.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "HTML title attribute for accessibility.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Disable the link.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    item: {
      control: "object",
      description: "Optional data passed to the onClick callback.",
      table: { type: { summary: "any" } },
    },
    onClick: {
      action: "clicked",
      description: "Callback invoked before opening the modal. Receives the item as parameter.",
      table: { type: { summary: "(item: any) => any" } },
    },
  },
} satisfies Meta<typeof ModalLink>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  args: {
    onClick: action("link clicked"),
  },
  render: (args) => (
    <div>
      <ModalLink {...args} />
      <Dialog id={args.target} title="Example Dialog" content={<p>This is the modal content.</p>} />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "Deprecated modal link component. Click the link to open the modal dialog.",
      },
    },
  },
};

export const WithIcon: Story = {
  args: {
    text: "View Details",
    icon: "fa-info-circle",
    target: "details-modal",
  },
  render: (args) => (
    <div>
      <ModalLink {...args} />
      <Dialog id={args.target} title="System Details" content={<p>System information would appear here.</p>} />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "Modal link with an icon.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    text: "Disabled Link",
    target: "disabled-modal",
    disabled: true,
  },
  render: (args) => (
    <div>
      <ModalLink {...args} />
      <Dialog id={args.target} title="Dialog" content={<p>This dialog won't open when disabled.</p>} />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "Disabled modal link that cannot be clicked.",
      },
    },
  },
};
