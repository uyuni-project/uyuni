import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Button } from "components/buttons";

import { Panel } from "./Panel";

const meta = {
  title: "Components/Panels/Panel",
  component: Panel,
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
    },
    collapseId: {
      control: "text",
      description: "Unique identifier that enables the collapsible panel body when provided.",
    },
    customIconClass: {
      control: "text",
      description: "Additional classes applied to the collapse chevrons.",
    },
    title: {
      control: "text",
      description: "Text displayed in the panel heading.",
    },
    className: {
      control: "text",
      description: "Panel variant or other CSS classes.",
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the title.",
    },
    header: {
      control: "text",
      description: "Additional content rendered inside the panel heading.",
    },
    footer: {
      control: "text",
      description: "Content rendered in the panel footer.",
    },
    children: {
      control: "text",
      description: "Main panel body content.",
    },
    buttons: {
      control: false,
      description: "Action content positioned on the right side of the heading.",
    },
    collapsClose: {
      control: "boolean",
      description: "Starts a collapsible panel closed when enabled.",
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
