import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { ExampleRow, StripedExampleSection } from "components/example-layout";

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
    },
    target: {
      control: "select",
      options: ["_self", "_blank", "_parent", "_top"],
      description: "Browsing context in which to open the link. `_blank` automatically receives a safe `rel` value.",
    },
    download: {
      control: "text",
      description: "Optional filename that makes the link download its target.",
    },
    handler: {
      action: "clicked",
      description: "Optional callback invoked when the anchor is clicked.",
    },
    text: {
      control: "text",
      description: "Visible link content. `children` can be used instead.",
    },
    children: {
      control: false,
      description: "Alternative content used when `text` is omitted.",
    },
    icon: {
      control: "text",
      description: "Font Awesome class displayed before the text.",
    },
    className: {
      control: "select",
      options: ["btn-primary", "btn-default", "btn-danger", "btn-tertiary"],
      description: "Uyuni button variant and optional additional CSS classes.",
    },
    title: {
      control: "text",
      description: "Accessible name and tooltip text.",
    },
    disabled: {
      control: "boolean",
      description: "Adds disabled styling to the link.",
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred tooltip placement.",
    },
  },
} satisfies Meta<typeof LinkButton>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const States: Story = {
  render: () => (
    <StripedExampleSection>
      <ExampleRow>
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
      </ExampleRow>
    </StripedExampleSection>
  ),
  parameters: {
    controls: { disable: true },
    docs: { description: { story: "Navigation, download, and disabled presentations." } },
  },
};
