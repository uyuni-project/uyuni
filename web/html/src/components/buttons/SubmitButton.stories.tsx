/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { SubmitButton } from "./index";

const meta = {
  title: "Components/Buttons/SubmitButton",
  component: SubmitButton,
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
      description: "Accessible name applied via aria-label. This component currently does not display a tooltip.",
    },
    disabled: {
      control: "boolean",
      description: "Prevents form submission when enabled.",
    },
    tooltipPlacement: {
      control: false,
      description: "Has no visible effect because SubmitButton does not forward tooltip content to the button.",
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
