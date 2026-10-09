import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Button } from "components/buttons";

import { InnerPanel } from "./InnerPanel";

const meta = {
  title: "Components/Panels/InnerPanel",
  component: InnerPanel,
  parameters: {
    docs: {
      description: {
        component:
          "Page-section panel with a heading icon, optional summary and help link, and left- or right-aligned toolbar actions.",
      },
    },
  },
  args: {
    title: "Managed systems",
    icon: "fa-desktop",
    summary: "Systems currently registered with this server.",
    children: "Panel content",
    helpUrl: "reference/systems/systems-menu.html",
  },
  argTypes: {
    title: {
      control: "text",
      description: "Section heading text.",
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the heading.",
    },
    buttons: {
      control: false,
      description: "Actions placed on the right side of the section toolbar.",
    },
    buttonsLeft: {
      control: false,
      description: "Actions placed on the left side of the section toolbar.",
    },
    children: {
      control: "text",
      description: "Main content displayed inside the bordered panel body.",
    },
    summary: {
      control: "text",
      description: "Optional explanatory content displayed below the heading.",
    },
    helpUrl: {
      control: "text",
      description: "Documentation path used to display a help link beside the heading.",
    },
  },
} satisfies Meta<typeof InnerPanel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const WithActions: Story = {
  args: {
    buttonsLeft: [<Button key="refresh" className="btn-default" icon="fa-refresh" text="Refresh" />],
    buttons: [
      <Button key="delete" className="btn-danger" icon="fa-trash" text="Delete" />,
      <Button key="create" className="btn-primary" icon="fa-plus" text="Create" />,
    ],
  },
  parameters: {
    docs: { description: { story: "Independent action groups placed on both sides of the section toolbar." } },
  },
};

export const WithoutOptionalContent: Story = {
  args: {
    summary: undefined,
    helpUrl: undefined,
  },
};
