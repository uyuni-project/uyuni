import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { DropdownButton } from "./index";

const dropdownItems = [
  <button className="dropdown-item" type="button" key="edit" onClick={action("edit clicked")}>
    Edit
  </button>,
  <button className="dropdown-item" type="button" key="duplicate" onClick={action("duplicate clicked")}>
    Duplicate
  </button>,
  <button className="dropdown-item" type="button" key="archive" onClick={action("archive clicked")}>
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
    },
    handler: {
      action: "clicked",
      description: "Optional callback invoked when the dropdown trigger is clicked.",
    },
    text: {
      control: "text",
      description: "Visible trigger content. `children` can be used instead.",
    },
    children: {
      control: false,
      description: "Alternative trigger content used when `text` is omitted.",
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the trigger text.",
    },
    className: {
      control: "select",
      options: ["btn-primary", "btn-default", "btn-danger", "btn-tertiary"],
      description: "Required Uyuni button variant and optional additional CSS classes.",
    },
    title: {
      control: "text",
      description: "Accessible name and tooltip text for the trigger.",
    },
    disabled: {
      control: "boolean",
      description: "Prevents the dropdown from being opened.",
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred tooltip placement.",
    },
  },
  render: (args) => (
    <div style={{ minHeight: "200px" }}>
      <DropdownButton {...args} />
    </div>
  ),
} satisfies Meta<typeof DropdownButton>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};
