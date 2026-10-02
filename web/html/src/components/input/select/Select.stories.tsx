import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { ExampleRow, StripedExampleSection } from "components/example-layout";

import { Select } from "./Select";

type OptionType = { label: string; value: string };

type SelectProps = React.ComponentProps<typeof Select<OptionType, string>>;

const StatefulSelect = (args: SelectProps) => {
  const [value, setValue] = useState<string | undefined>(args.value);

  useEffect(() => {
    setValue(args.value);
  }, [args.value]);

  return (
    <div>
      <Select
        {...args}
        value={value}
        onChange={(newValue) => {
          setValue(newValue);
          action("changed")(newValue);
        }}
      />
      {value !== undefined && (
        <p style={{ marginTop: "1rem" }}>
          Selected value: <code>{JSON.stringify(value)}</code>
        </p>
      )}
    </div>
  );
};

const StatefulMultiSelect = (args: any) => {
  const [value, setValue] = useState<string[]>(args.value || []);

  useEffect(() => {
    setValue(args.value || []);
  }, [args.value]);

  return (
    <div>
      <Select
        {...args}
        value={value}
        onChange={(newValue) => {
          setValue(newValue || []);
          action("changed")(newValue);
        }}
      />
      {value.length > 0 && (
        <p style={{ marginTop: "1rem" }}>
          Selected values: <code>{JSON.stringify(value)}</code>
        </p>
      )}
    </div>
  );
};

const userOptions = [
  { label: "John Doe", value: "john" },
  { label: "Jane Smith", value: "jane" },
  { label: "Bob Johnson", value: "bob" },
  { label: "Alice Williams", value: "alice" },
  { label: "Charlie Brown", value: "charlie" },
];

const priorityOptions = [
  { label: "Low", value: "low" },
  { label: "Normal", value: "normal" },
  { label: "High", value: "high" },
  { label: "Critical", value: "critical" },
];

const meta = {
  title: "Components/Inputs/Select",
  component: Select,
  parameters: {
    docs: {
      description: {
        component:
          "Dropdown select component powered by react-select. Supports single and multiple selection, async data loading, pagination, custom option formatting, and integration with the Uyuni form system.",
      },
    },
  },
  decorators: [
    (Story) => (
      <div style={{ minHeight: "400px", padding: "20px", maxWidth: "600px" }}>
        <Story />
      </div>
    ),
  ],
  args: {
    options: userOptions,
    value: undefined,
    placeholder: "Select user...",
    isClearable: true,
    disabled: false,
    isLoading: false,
  },
  argTypes: {
    options: {
      control: "object",
      description: "Array of options to display in the dropdown.",
    },
    value: {
      control: "text",
      description: "Currently selected value. For multi-select, this is an array of values.",
    },
    onChange: {
      action: "changed",
      description:
        "Callback invoked when selection changes. For single select receives the value, for multi-select receives an array of values.",
    },
    placeholder: {
      control: "text",
      description: "Placeholder text displayed when no option is selected.",
    },
    isClearable: {
      control: "boolean",
      description: "Allow clearing the selected value by clicking the clear button.",
    },
    isMulti: {
      control: "boolean",
      description: "Enable multi-select mode allowing selection of multiple options.",
    },
    disabled: {
      control: "boolean",
      description: "Disable the select dropdown.",
    },
    isLoading: {
      control: "boolean",
      description: "Show a loading indicator inside the dropdown.",
    },
    name: {
      control: "text",
      description: "Name attribute for the select input.",
    },
    label: {
      control: "text",
      description: "ARIA label for accessibility when no visual label is present.",
    },
    className: {
      control: "text",
      description: "Additional CSS classes applied to the select container.",
    },
    getOptionValue: {
      control: false,
      description: "Function to extract the value from an option object. Default: `option => option.value`",
    },
    getOptionLabel: {
      control: false,
      description: "Function to extract the label from an option object. Default: `option => option.label`",
    },
    formatOptionLabel: {
      control: false,
      description: "Custom renderer for option labels in the menu and control. Receives option and metadata.",
    },
    onBlur: {
      action: "blurred",
      description: "Callback invoked when the select loses focus.",
    },
    loadOptions: {
      control: false,
      description:
        "Async function to load options dynamically. Receives search string and returns a promise of options.",
    },
    cacheOptions: {
      control: "boolean",
      description: "Cache loaded async options to avoid redundant requests.",
    },
    paginate: {
      control: "boolean",
      description: "Enable pagination for async options loading.",
    },
    defaultValueOption: {
      control: false,
      description: "Default option object for async selects when the value is pre-selected.",
    },
    "data-testid": {
      control: "text",
      description: "Test ID for automated testing.",
    },
  },
  render: (args) => <StatefulSelect {...args} />,
} satisfies Meta<typeof Select>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive select dropdown. Choose an option to see the selected value update below.",
      },
    },
  },
};

export const Clearable: Story = {
  args: {
    options: priorityOptions,
    value: "normal",
    placeholder: "Select priority...",
    isClearable: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Select with clearable option. Click the × button to clear the selection.",
      },
    },
  },
};

export const NotClearable: Story = {
  args: {
    options: priorityOptions,
    value: "normal",
    placeholder: "Select priority...",
    isClearable: false,
  },
  parameters: {
    docs: {
      description: {
        story: "Select without clear button. Once selected, a value must always be chosen.",
      },
    },
  },
};

export const MultiSelect: Story = {
  args: {
    options: userOptions,
    value: ["john", "jane"],
    placeholder: "Select users...",
    isMulti: true,
    isClearable: true,
  },
  render: (args) => <StatefulMultiSelect {...args} />,
  parameters: {
    docs: {
      description: {
        story: "Multi-select mode allowing selection of multiple options. Selected items appear as tags.",
      },
    },
  },
};

export const Disabled: Story = {
  args: {
    options: userOptions,
    value: "john",
    disabled: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Disabled select dropdown that cannot be interacted with.",
      },
    },
  },
};

export const Loading: Story = {
  args: {
    options: [],
    placeholder: "Loading options...",
    isLoading: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Select showing loading indicator while options are being fetched.",
      },
    },
  },
};

export const AsyncSelect: Story = {
  args: {
    placeholder: "Search users...",
    isClearable: true,
    loadOptions: async (searchString: string) => {
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 500));

      // Filter options based on search
      return userOptions.filter((option) => option.label.toLowerCase().includes(searchString.toLowerCase()));
    },
    cacheOptions: true,
  },
  parameters: {
    docs: {
      description: {
        story:
          "Async select that loads options dynamically. Start typing to search for users. Results are cached to avoid redundant requests.",
      },
    },
  },
};

export const CustomOptionRendering: Story = {
  args: {
    options: [
      { label: "Admin", value: "admin", icon: "fa-shield" },
      { label: "User", value: "user", icon: "fa-user" },
      { label: "Guest", value: "guest", icon: "fa-eye" },
    ] as any,
    placeholder: "Select role...",
    formatOptionLabel: (option: any) => (
      <div>
        <i className={`fa ${option.icon}`} style={{ marginRight: "8px" }} />
        {option.label}
      </div>
    ),
  },
  parameters: {
    docs: {
      description: {
        story: "Select with custom option rendering. Each option displays an icon alongside the label.",
      },
    },
  },
};

export const LongList: Story = {
  args: {
    options: Array.from({ length: 100 }, (_, i) => ({
      label: `System ${i + 1}`,
      value: `system-${i + 1}`,
    })),
    placeholder: "Select system...",
    isClearable: true,
  },
  parameters: {
    docs: {
      description: {
        story: "Select with a long list of options. The dropdown is scrollable and performs well with many items.",
      },
    },
  },
};

export const Variants: Story = {
  render: () => (
    <StripedExampleSection>
      <ExampleRow>
        <div style={{ width: "300px" }}>
          <p>
            <strong>Normal</strong>
          </p>
          <Select options={priorityOptions} value="normal" isClearable={false} />
        </div>
        <div style={{ width: "300px" }}>
          <p>
            <strong>With placeholder</strong>
          </p>
          <Select options={priorityOptions} placeholder="Choose priority..." isClearable />
        </div>
        <div style={{ width: "300px" }}>
          <p>
            <strong>Disabled</strong>
          </p>
          <Select options={priorityOptions} value="high" disabled />
        </div>
      </ExampleRow>
    </StripedExampleSection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Different select states shown side by side.",
      },
    },
  },
};
