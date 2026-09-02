import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { LinkButton } from "./index";

const meta = {
  title: "Components/Buttons/LinkButton",
  component: LinkButton,
  parameters: {
    docs: {
      description: {
        component:
          "Anchor element styled as a Uyuni button. Use it for navigation or downloads, and use `Button` for in-page actions.",
      },
    },
  },
  args: {
    href: "#link-button-example",
    text: "View details",
    icon: "fa-external-link",
    className: "btn-default",
    title: "View details",
    target: "_self",
    disabled: false,
  },
  argTypes: {
    href: {
      control: "text",
      description: "Destination assigned to the anchor's `href` attribute.",
      table: { type: { summary: "string" } },
    },
    target: {
      control: "select",
      options: ["_self", "_blank", "_parent", "_top"],
      description: "Browsing context in which to open the link. `_blank` automatically receives a safe `rel` value.",
      table: { type: { summary: "string" } },
    },
    download: {
      control: "text",
      description: "Optional filename that makes the link download its target.",
      table: { type: { summary: "string" } },
    },
    handler: {
      action: "clicked",
      description: "Optional callback invoked when the anchor is clicked.",
      table: { type: { summary: "(...args: any[]) => any" } },
    },
    text: {
      control: "text",
      description: "Visible link content. `children` can be used instead.",
      table: { type: { summary: "ReactNode" } },
    },
    children: {
      control: false,
      description: "Alternative content used when `text` is omitted.",
      table: { type: { summary: "ReactNode" } },
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the text.",
      table: { type: { summary: "string" } },
    },
    className: {
      control: "select",
      options: ["btn-primary", "btn-default", "btn-danger", "btn-tertiary"],
      description: "Uyuni button variant and optional additional CSS classes.",
      table: { type: { summary: "string" }, defaultValue: { summary: "btn-default" } },
    },
    title: {
      control: "text",
      description: "Accessible name and tooltip text.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Adds disabled styling to the link.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred tooltip placement.",
      table: { type: { summary: '"top" | "right" | "bottom" | "left"' } },
    },
  },
} satisfies Meta<typeof LinkButton>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <LinkButton href="#primary" className="btn-primary" text="Primary link" />
        <LinkButton href="#default" className="btn-default" text="Default link" />
        <LinkButton
          href="#download"
          className="btn-tertiary"
          icon="fa-download"
          text="Download"
          download="report.csv"
        />
        <LinkButton href="#disabled" className="btn-default" text="Disabled link" disabled />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Navigation, download, and disabled presentations." } },
  },
};
