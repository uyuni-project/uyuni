import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { SubmitButton } from "./index";

const meta = {
  title: "Components/Buttons/SubmitButton",
  component: SubmitButton,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component: 'Uyuni button with native `type="submit"`, intended for the primary submission action in a form.',
      },
    },
  },
  args: {
    text: "Submit form",
    icon: "fa-check",
    className: "btn-primary",
    title: "Submit form",
    disabled: false,
  },
  argTypes: {
    text: {
      control: "text",
      description: "Visible button content. `children` can be used instead.",
      table: { type: { summary: "ReactNode" } },
    },
    children: {
      control: "text",
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
      table: { type: { summary: "string" } },
    },
    title: {
      control: "text",
      description: "Accessible name and tooltip text.",
      table: { type: { summary: "string" } },
    },
    disabled: {
      control: "boolean",
      description: "Prevents form submission when enabled.",
      table: { type: { summary: "boolean" }, defaultValue: { summary: "false" } },
    },
    tooltipPlacement: {
      control: "select",
      options: ["top", "right", "bottom", "left"],
      description: "Preferred tooltip placement.",
      table: { type: { summary: '"top" | "right" | "bottom" | "left"' } },
    },
  },
  render: (args) => (
    <form onSubmit={(event) => event.preventDefault()}>
      <SubmitButton {...args} />
    </form>
  ),
} satisfies Meta<typeof SubmitButton>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};
