/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Button } from "components/buttons";

import { useSelected } from "./useSelected";

const rows = [
  { id: 1, name: "Base channel", children: [{ id: 2, name: "Updates channel", children: [] }] },
  { id: 3, name: "Independent channel", children: [] },
];

const SelectionExample = () => {
  const selection = useSelected(
    (row: (typeof rows)[number]) => row.id,
    async () => [1, 2, 3],
    undefined,
    [],
    action("selection changed")
  );

  return (
    <div>
      <p>Selecting the base channel also selects its child. Select all includes every row.</p>
      {rows.map((row) => (
        <div key={row.id}>
          <label>
            <input type="checkbox" checked={selection.isSelected(row)} onChange={() => selection.toggle(row)} />
            {row.name}
          </label>
          {row.children.map((child) => (
            <label key={child.id} style={{ display: "block", marginLeft: "24px" }}>
              <input type="checkbox" checked={selection.isSelected(child)} onChange={() => selection.toggle(child)} />
              {child.name}
            </label>
          ))}
        </div>
      ))}
      <Button text="Select all" handler={() => selection.toggleSelectAll()} />
      <Button text="Clear selection" handler={selection.clear} />
      <p>Selected IDs: {selection.getSelected().join(", ") || "none"}</p>
    </div>
  );
};

const meta = {
  title: "Components/Tables/useSelected",
  component: SelectionExample,
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        component:
          "Selection hook used by ChannelSelectorTable. The default ChildrenSelectionProvider selects descendants together with their parent. The async identifier callback supports selection across pages.",
      },
    },
  },
} satisfies Meta<typeof SelectionExample>;

export default meta;
type Story = StoryObj<typeof meta>;
export const Playground: Story = {};
