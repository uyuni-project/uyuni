import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { SelectedRowDetails } from "./SelectedRowDetails";

type SelectedRowDetailsProps = React.ComponentProps<typeof SelectedRowDetails>;

const StatefulSelectedRowDetails = (props: SelectedRowDetailsProps) => {
  const [selectedCount, setSelectedCount] = useState(props.selectedCount ?? 0);

  useEffect(() => setSelectedCount(props.selectedCount ?? 0), [props.selectedCount]);

  return (
    <SelectedRowDetails
      {...props}
      selectedCount={selectedCount}
      onClear={() => {
        setSelectedCount(0);
        props.onClear();
      }}
      onSelectAll={() => {
        setSelectedCount(props.itemCount);
        props.onSelectAll();
      }}
    />
  );
};

const meta = {
  title: "Components/Tables/SelectedRowDetails",
  component: SelectedRowDetails,
  parameters: {
    docs: {
      description: {
        component:
          "Summarizes a table selection and provides actions to select the entire filtered result or clear the selection.",
      },
    },
  },
  args: {
    itemCount: 25,
    selectedCount: 3,
    selectable: true,
    onClear: action("selection cleared"),
    onSelectAll: action("all selected"),
  },
  argTypes: {
    itemCount: {
      control: { type: "number", min: 0, step: 1 },
      description: "Total number of filtered table rows available for selection.",
    },
    selectedCount: {
      control: { type: "number", min: 0, step: 1 },
      description: "Number of rows currently selected.",
    },
    selectable: {
      control: "boolean",
      description: "Displays selection details when selection is enabled and at least one row is selected.",
    },
    onClear: {
      action: "selection cleared",
      description: "Called when the user clears the current selection.",
    },
    onSelectAll: {
      action: "all selected",
      description: "Called when the user selects every filtered row.",
    },
  },
  render: (args) => <StatefulSelectedRowDetails {...args} />,
} satisfies Meta<typeof SelectedRowDetails>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const AllSelected: Story = {
  args: {
    selectedCount: 25,
  },
};
