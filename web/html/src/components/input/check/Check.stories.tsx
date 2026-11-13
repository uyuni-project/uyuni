/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Check } from "./Check";

const ControlledCheck = (args: React.ComponentProps<typeof Check>) => {
  const [checked, setChecked] = useState(args.checked ?? false);
  const [indeterminate, setIndeterminate] = useState(args.indeterminate ?? false);
  return (
    <Check
      {...args}
      checked={checked}
      indeterminate={indeterminate}
      onChange={(value) => {
        setChecked(value);
        setIndeterminate(false);
        args.onChange?.(value);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/Check",
  component: Check,
  parameters: {
    docs: {
      description: {
        component:
          "Standalone checkbox with the standard Uyuni label wrapper and input styling. Use CheckInput when providing a custom label wrapper, or Field.Check inside a Formik form.",
      },
    },
  },
  args: {
    "aria-label": "Select item",
    checked: false,
    indeterminate: false,
    disabled: false,
    onChange: action("changed"),
  },
  argTypes: {
    checked: { control: "boolean" },
    indeterminate: { control: "boolean" },
    disabled: { control: "boolean" },
    inputClass: { control: "text", description: "Overrides the input styling; defaults to form-check-input." },
  },
  render: (args) => <ControlledCheck key={JSON.stringify([args.checked, args.indeterminate])} {...args} />,
} satisfies Meta<typeof Check>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
export const Indeterminate: Story = { args: { indeterminate: true } };
export const Disabled: Story = { args: { disabled: true, checked: true } };
