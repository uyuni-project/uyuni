import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button } from "./index";

const buttonClassOptions = [
  "btn-primary",
  "btn-default",
  "btn-danger",
  "btn-tertiary",
  "btn-primary btn-sm",
  "btn-default btn-sm",
  "btn-danger btn-sm",
  "btn-tertiary btn-sm",
];

const iconOptions = [
  "fa-plus",
  "fa-trash",
  "fa-pencil",
  "fa-download",
  "fa-check",
  "fa-times",
  "fa-refresh",
  "fa-floppy-o",
  "fa-chevron-left",
  "fa-search",
  "fa-list",
  "fa-edit",
];

const tooltipPlacementOptions = ["top", "right", "bottom", "left"];

const meta = {
  title: "Components/Buttons/Button",
  component: Button,
  parameters: {
    docs: {
      description: {
        component:
          "Uyuni button wrapper around a native `<button>`. The base `btn` class is prepended automatically; pass a Bootstrap/Uyuni modifier through `className` (`btn-primary`, `btn-default`, `btn-danger`, `btn-tertiary`, plus optional `btn-sm`).",
      },
    },
  },
  args: {
    className: "btn-primary",
    text: "Create",
    title: "Create item",
    icon: "fa-plus",
    tooltipPlacement: "top",
    disabled: false,
    handler: action("clicked"),
  },
  argTypes: {
    className: {
      control: "select",
      options: buttonClassOptions,
      description: "Uyuni button variant and optional size classes. The base `btn` class is added automatically.",
      table: { type: { summary: "string" }, defaultValue: { summary: "btn-default" } },
    },
    text: {
      control: "text",
      description: "Visible button content. `children` can be used instead.",
      table: { type: { summary: "ReactNode" } },
    },
    children: {
      control: false,
      description: "Alternative button content used when `text` is omitted.",
      table: { type: { summary: "ReactNode" } },
    },
    id: {
      control: "text",
      description: "Optional HTML identifier assigned to the button.",
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Accessible name and native tooltip text.",
      table: { type: { summary: "string" } },
    },
    icon: {
      control: "select",
      options: iconOptions,
      description: "Font Awesome class displayed before the button content.",
      table: { type: { summary: "string" } },
    },
    tooltipPlacement: {
      control: "select",
      options: tooltipPlacementOptions,
      description: "Preferred Bootstrap tooltip placement.",
      table: { type: { summary: '"top" | "right" | "bottom" | "left"' } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents the button from being activated.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    handler: {
      action: "clicked",
      description: "Callback invoked when the button is activated.",
      table: { type: { summary: "(...args: any[]) => any" } },
    },
  },
} satisfies Meta<typeof Button>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Use the controls to try out `className`, `text`, `icon`, `title`, and the disabled state.",
      },
    },
  },
};

export const Variants: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Use the button variant that matches the importance and impact of the action.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Button className="btn-primary" text="Primary" />
        <Button className="btn-default" text="Default" />
        <Button className="btn-danger" text="Danger" />
        <Button className="btn-tertiary" text="Tertiary" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const Icons: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Buttons can combine an icon and a label or display an icon alone. Icon-only buttons must have a descriptive `title` so their action remains accessible.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Button className="btn-primary" title="Add" icon="fa-plus" />
        <Button className="btn-default" title="Delete" icon="fa-trash" />
        <Button className="btn-primary" icon="fa-plus" text="Primary" />
        <Button className="btn-default" icon="fa-plus" text="Default" />
        <Button className="btn-tertiary" icon="fa-plus" text="Tertiary" />
        <Button className="btn-tertiary" title="Delete" icon="fa-trash" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const Sizes: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Buttons support the default size and a small size. Add `btn-sm` for small buttons.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Button className="btn-primary" text="Default button" />
        <Button className="btn-primary btn-sm" text="Small button" />
        <Button className="btn-default btn-sm" text="Small button" />
        <Button className="btn-danger btn-sm" text="Small button" />
        <Button className="btn-default btn-sm" title="Delete" icon="fa-trash" />
        <Button className="btn-primary btn-sm" title="Add" icon="fa-plus" />
        <Button className="btn-tertiary btn-sm" title="Delete" icon="fa-trash" />
      </StoryRow>
    </StripedStorySection>
  ),
};

export const Disabled: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Set the `disabled` boolean property to make a button unavailable.",
      },
    },
  },
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Button className="btn-primary" text="Primary" disabled />
        <Button className="btn-default" text="Default" disabled />
        <Button className="btn-danger" text="Danger" disabled />
        <Button className="btn-tertiary" text="Tertiary" disabled />
      </StoryRow>
    </StripedStorySection>
  ),
};
