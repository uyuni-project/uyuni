import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Combobox, ComboboxItem } from "./combobox";

type ComboboxProps = React.ComponentProps<typeof Combobox>;

const options: ComboboxItem[] = [
  { id: 1, text: "Development" },
  { id: 2, text: "Quality assurance" },
  { id: 3, text: "Production" },
];

const StatefulCombobox = (props: ComboboxProps) => {
  const [selectedId, setSelectedId] = useState(props.selectedId);

  useEffect(() => setSelectedId(props.selectedId), [props.selectedId]);

  return (
    <Combobox
      {...props}
      selectedId={selectedId}
      onSelect={(item) => {
        setSelectedId(item.id);
        props.onSelect(item);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/Combobox",
  component: Combobox,
  parameters: {
    docs: {
      description: {
        component:
          "Creatable select input that accepts an existing item or lets the user enter a new value. Selections are returned in Uyuni's `{ id, text }` shape.",
      },
    },
  },
  args: {
    id: "storybook-combobox",
    name: "environment",
    options,
    selectedId: 1,
    placeholder: "Select or create an environment",
    onSelect: action("selected"),
    onFocus: action("focused"),
  },
  argTypes: {
    id: {
      control: "text",
      description: "HTML identifier assigned to the underlying select.",
      table: { type: { summary: "string" } },
    },
    name: {
      control: "text",
      description: "Form field name assigned to the underlying select.",
      table: { type: { summary: "string" } },
    },
    options: {
      control: "object",
      description: "Existing values offered by the combobox.",
      table: { type: { summary: "ComboboxItem[]" } },
    },
    selectedId: {
      control: "select",
      options: [1, 2, 3],
      description: "Identifier of the currently selected item.",
      table: { type: { summary: "number | string | null" } },
    },
    placeholder: {
      control: "text",
      description: "Hint displayed when no item is selected.",
      table: { type: { summary: "string" } },
    },
    onFocus: {
      action: "focused",
      description: "Called when the combobox receives focus.",
      table: { type: { summary: "() => void" } },
    },
    onSelect: {
      action: "selected",
      description: "Called with the selected or newly created `{ id, text }` item.",
      table: { type: { summary: "(value: ComboboxItem) => void" } },
    },
    getNewOptionData: {
      control: false,
      description: "Optional factory used by react-select to create a new option object from user input.",
      table: { type: { summary: "(userInput: string, label: string) => object" } },
    },
    "data-testid": {
      control: "text",
      description: "Base identifier used to create test attributes for the select's internal elements.",
      table: { type: { summary: "string" } },
    },
  },
  decorators: [
    (Story) => (
      <div style={{ minHeight: "400px", padding: "20px" }}>
        <Story />
      </div>
    ),
  ],
  render: (args) => <StatefulCombobox {...args} />,
} satisfies Meta<typeof Combobox>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story:
          "Interactive combobox that allows selecting from existing options or creating new ones. Click to open the dropdown.",
      },
    },
  },
};

export const Empty: Story = {
  args: {
    options: [],
    selectedId: undefined,
    placeholder: "Type to create the first option",
  },
  parameters: {
    docs: {
      description: {
        story: "Combobox with no pre-existing options. Type to create a new option.",
      },
    },
  },
};

export const ManyOptions: Story = {
  args: {
    options: [
      { id: 1, text: "Development" },
      { id: 2, text: "Quality Assurance" },
      { id: 3, text: "Staging" },
      { id: 4, text: "Production" },
      { id: 5, text: "Testing" },
      { id: 6, text: "Pre-production" },
      { id: 7, text: "Demo" },
      { id: 8, text: "Research" },
    ],
    selectedId: 1,
  },
  parameters: {
    docs: {
      description: {
        story: "Combobox with multiple options. The dropdown is scrollable when many options are available.",
      },
    },
  },
};

export const NoSelection: Story = {
  args: {
    selectedId: undefined,
    placeholder: "Choose an environment or create a new one",
  },
  parameters: {
    docs: {
      description: {
        story: "Combobox with no initial selection, showing the placeholder.",
      },
    },
  },
};
