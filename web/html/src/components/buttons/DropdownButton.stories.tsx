import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { DropdownButton } from "./index";

const dropdownItems = [
  <a className="dropdown-item" href="#edit" key="edit">
    Edit
  </a>,
  <a className="dropdown-item" href="#duplicate" key="duplicate">
    Duplicate
  </a>,
  <button className="dropdown-item" type="button" key="archive">
    Archive
  </button>,
];

const meta = {
  title: "Components/Buttons/DropdownButton",
  component: DropdownButton,
  parameters: {
    docs: {
      description: {
        component: "Uyuni button that opens a Bootstrap dropdown containing caller-provided menu items.",
      },
    },
  },
  args: {
    text: "Actions",
    icon: "fa-cog",
    className: "btn-default",
    title: "Available actions",
    items: dropdownItems,
    disabled: false,
  },
  argTypes: {
    items: {
      control: false,
      description: "Menu elements rendered as list items inside the dropdown.",
      table: { type: { summary: "ReactNode[]" } },
    },
    handler: {
      action: "clicked",
      description: "Optional callback invoked when the dropdown trigger is clicked.",
      table: { type: { summary: "(...args: any[]) => any" } },
    },
    text: {
      control: "text",
      description: "Visible trigger content. `children` can be used instead.",
      table: { type: { summary: "ReactNode" } },
    },
    children: {
      control: "text",
      description: "Alternative trigger content used when `text` is omitted.",
      table: { type: { summary: "ReactNode" } },
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the trigger text.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "select",
      options: ["btn-primary", "btn-default", "btn-danger", "btn-tertiary"],
      description: "Required Uyuni button variant and optional additional CSS classes.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Accessible name and tooltip text for the trigger.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents the dropdown from being opened.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred tooltip placement.",
      table: { type: { summary: '"top" | "right" | "bottom" | "left"' } },
    },
  },
} satisfies Meta<typeof DropdownButton>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};
