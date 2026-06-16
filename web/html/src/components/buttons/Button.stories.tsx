import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Button } from "./index";

const meta = {
  title: "Components/Buttons/Button",
  component: Button,
  tags: ["autodocs"],
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
    disabled: false,
  },
  argTypes: {
    className: { control: "text" },
    text: { control: "text" },
    title: { control: "text" },
    icon: { control: "text" },
    disabled: { control: "boolean" },
    handler: { action: "clicked" },
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

export const IconOnly: Story = {
  args: {
    className: "btn-danger",
    text: "",
    icon: "fa-trash",
    title: "Delete item",
  },
  parameters: {
    docs: {
      description: {
        story:
          "Icon-only buttons must set `title` so the action stays accessible. The wrapper also wires it up as a tooltip.",
      },
    },
  },
};

export const Variants: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: { story: "Reference of the four supported variants in default and small size." },
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
      <StoryRow>
        <Button className="btn-primary btn-sm" text="Primary" />
        <Button className="btn-default btn-sm" text="Default" />
        <Button className="btn-danger btn-sm" text="Danger" />
        <Button className="btn-tertiary btn-sm" text="Tertiary" />
      </StoryRow>
    </StripedStorySection>
  ),
};
