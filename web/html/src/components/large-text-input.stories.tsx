import { useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Form } from "components/input";

import { LargeTextInput, LargeTextInputMode } from "./large-text-input";

type LargeTextInputProps = React.ComponentProps<typeof LargeTextInput>;
type StoryProps = LargeTextInputProps & { initialMode?: LargeTextInputMode };

const StatefulLargeTextInput = ({ initialMode, ...props }: StoryProps) => {
  const [model, setModel] = useState<Record<string, unknown>>(
    initialMode ? { [`${props.name}_inputMode`]: initialMode } : {}
  );

  return (
    <Form model={model} onChange={(nextModel) => setModel({ ...nextModel })} title="Large text input example">
      <LargeTextInput {...props} />
    </Form>
  );
};

const meta = {
  title: "Components/Inputs/LargeTextInput",
  component: LargeTextInput,
  parameters: {
    docs: {
      description: {
        component:
          "Mode selector for supplying a large text payload. The user can paste text into a textarea, upload a text file, or—when optional—provide no data.",
      },
    },
  },
  args: {
    name: "configuration",
    required: false,
    label: "Configuration data",
    hint: "Choose how to provide the configuration.",
    notNeededOptionLabel: "Not needed",
    uploadOptionLabel: "Upload a file",
    pasteOptionLabel: "Paste the data",
    uploadLabel: "Configuration file",
    uploadHint: "Select a UTF-8 text file.",
    pasteLabel: "Configuration content",
    pasteHint: "Paste the complete configuration.",
    pastePlaceholder: "key: value",
  },
  argTypes: {
    name: {
      control: "text",
      description: "Prefix used for the mode, upload, and pasted-data form-model keys.",
    },
    required: {
      control: "boolean",
      description: "Removes the `Not needed` choice and initially selects file upload when enabled.",
    },
    label: {
      control: "text",
      description: "Label displayed for the input-mode choices.",
    },
    hint: {
      control: "text",
      description: "Supporting text displayed below the input-mode choices.",
    },
    notNeededOptionLabel: {
      control: "text",
      description: "Custom label for the optional no-data mode.",
    },
    uploadOptionLabel: {
      control: "text",
      description: "Custom label for the file-upload mode.",
    },
    pasteOptionLabel: {
      control: "text",
      description: "Custom label for the pasted-data mode.",
    },
    uploadLabel: {
      control: "text",
      description: "Label displayed beside the file input.",
    },
    uploadHint: {
      control: "text",
      description: "Supporting text displayed below the file input.",
    },
    pasteLabel: {
      control: "text",
      description: "Label displayed beside the paste textarea.",
    },
    pasteHint: {
      control: "text",
      description: "Supporting text displayed below the paste textarea.",
    },
    pastePlaceholder: {
      control: "text",
      description: "Placeholder displayed in the paste textarea.",
    },
  },
  render: (args) => <StatefulLargeTextInput key={`${args.name}-${args.required}`} {...args} />,
} satisfies Meta<typeof LargeTextInput>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: (args) => (
    <StatefulLargeTextInput key={`${args.name}-${args.required}`} {...args} initialMode={LargeTextInputMode.Paste} />
  ),
  parameters: {
    docs: {
      description: {
        story: "Starts in paste mode so the primary editable textarea is immediately visible.",
      },
    },
  },
};

export const FileUpload: Story = {
  render: (args) => (
    <StatefulLargeTextInput key={`${args.name}-${args.required}`} {...args} initialMode={LargeTextInputMode.Upload} />
  ),
  parameters: {
    docs: { description: { story: "The same large text value can be supplied from a local text file." } },
  },
};

export const OptionalNoData: Story = {
  parameters: {
    docs: { description: { story: "Optional inputs can explicitly indicate that no data should be supplied." } },
  },
};

export const Required: Story = {
  args: {
    required: true,
  },
  parameters: {
    docs: { description: { story: "Required inputs offer only upload and paste modes and start in upload mode." } },
  },
};
