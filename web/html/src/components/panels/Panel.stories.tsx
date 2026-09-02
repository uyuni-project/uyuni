import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Button } from "components/buttons";

import { Panel } from "./Panel";

const meta = {
  title: "Components/Panels/Panel",
  component: Panel,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "General-purpose Uyuni panel with optional title, icon, header content, action buttons, footer, and collapsible body.",
      },
    },
  },
  args: {
    headingLevel: "h2",
    title: "System details",
    icon: "fa-desktop",
    className: "panel-default",
    children: "Panel body content",
    collapsClose: false,
  },
  argTypes: {
    headingLevel: {
      control: "select",
      options: ["h1", "h2", "h3", "h4", "h5", "h6"],
      description: "HTML heading element used for the panel title.",
      table: { type: { summary: "keyof JSX.IntrinsicElements" }, defaultValue: { summary: "h1" } },
    },
    collapseId: {
      control: "text",
      description: "Unique identifier that enables the collapsible panel body when provided.",
      table: { type: { summary: "string | null" } },
    },
    customIconClass: {
      control: "text",
      description: "Additional classes applied to the collapse chevrons.",
      table: { type: { summary: "string | null" } },
    },
    title: {
      control: "text",
      description: "Text displayed in the panel heading.",
      table: { type: { summary: "string | null" } },
    },
    className: {
      control: "text",
      description: "Panel variant or other CSS classes.",
      table: { type: { summary: "string" }, defaultValue: { summary: "panel-default" } },
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the title.",
      table: { type: { summary: "string | null" } },
    },
    header: {
      control: "text",
      description: "Additional content rendered inside the panel heading.",
      table: { type: { summary: "ReactNode" } },
    },
    footer: {
      control: "text",
      description: "Content rendered in the panel footer.",
      table: { type: { summary: "ReactNode" } },
    },
    children: {
      control: "text",
      description: "Main panel body content.",
      table: { type: { summary: "ReactNode" } },
    },
    buttons: {
      control: false,
      description: "Action content positioned on the right side of the heading.",
      table: { type: { summary: "ReactNode" } },
    },
    collapsClose: {
      control: "boolean",
      description: "Starts a collapsible panel closed when enabled.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
  },
} satisfies Meta<typeof Panel>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const WithActionsAndFooter: Story = {
  args: {
    buttons: <Button className="btn-primary btn-sm" icon="fa-pencil" text="Edit" />,
    footer: "Last updated just now",
  },
};

export const Collapsible: Story = {
  args: {
    collapseId: "storybook-panel",
    title: "Collapsible panel",
    children: "Use the heading to expand or collapse this content.",
  },
};
