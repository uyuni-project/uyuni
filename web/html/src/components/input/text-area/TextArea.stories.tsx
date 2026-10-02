import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { Form } from "../form/Form";
import { TextArea } from "./TextArea";

type TextAreaProps = React.ComponentProps<typeof TextArea>;

const StatefulTextArea = (props: TextAreaProps) => {
  const [model, setModel] = useState<Record<string, string>>({ [props.name]: props.defaultValue ?? "" });

  useEffect(() => setModel({ [props.name]: props.defaultValue ?? "" }), [props.defaultValue, props.name]);

  return (
    <Form model={model} onChange={(nextModel) => setModel({ ...nextModel })} title="Text area example">
      <TextArea {...props} />
    </Form>
  );
};

const meta = {
  title: "Components/Inputs/TextArea",
  component: TextArea,
  parameters: {
    docs: {
      description: {
        component:
          "Multi-line field for the legacy controlled `Form`, with consistent labels, hints, validation, and layout.",
      },
    },
  },
  args: {
    name: "description",
    defaultValue: "A managed system used for Storybook examples.",
    label: "Description",
    title: "System description",
    hint: "Add enough detail to identify this system.",
    rows: 5,
    cols: 60,
    placeholder: "Enter a description",
    inputClass: "",
    labelClass: "col-md-3",
    divClass: "col-md-6",
    className: "",
    hideLabel: false,
    required: false,
    disabled: false,
    autoComplete: "off",
    onChange: action("value changed"),
  },
  argTypes: {
    name: {
      control: "text",
      description: "Required form-model key and HTML name/id for the textarea.",
    },
    defaultValue: {
      control: "text",
      description: "Initial value used when the form model has no value for `name`.",
    },
    label: {
      control: "text",
      description: "Form label displayed next to the textarea.",
    },
    title: {
      control: "text",
      description: "Native title assigned to the textarea.",
    },
    hideLabel: {
      control: "boolean",
      description: "Hides the label while retaining the rest of the field layout.",
    },
    hint: {
      control: "text",
      description: "Supporting content displayed below the textarea.",
    },
    labelClass: {
      control: "text",
      description: "CSS classes applied to the field label.",
    },
    divClass: {
      control: "text",
      description: "CSS classes applied to the input-side wrapper.",
    },
    className: {
      control: "text",
      description: "CSS classes applied to the outer form group.",
    },
    required: {
      control: "boolean",
      description: "Marks the field as required and enables required-value validation.",
    },
    disabled: {
      control: "boolean",
      description: "Prevents editing when enabled.",
    },
    validators: {
      control: false,
      description: "Synchronous or asynchronous validation function, or an array of validators.",
    },
    invalidHint: {
      control: "text",
      description: "Message displayed when custom or required validation fails.",
    },
    onChange: {
      action: "value changed",
      description: "Called with the model key and new textarea value.",
    },
    autoComplete: {
      control: "text",
      description: "Browser autocomplete hint passed through the field.",
    },
    rows: {
      control: { type: "number", min: 1, step: 1 },
      description: "Visible number of text rows.",
    },
    cols: {
      control: { type: "number", min: 1, step: 1 },
      description: "Suggested visible width measured in character columns.",
    },
    placeholder: {
      control: "text",
      description: "Hint displayed while the textarea is empty.",
    },
    inputClass: {
      control: "text",
      description: "Additional CSS classes applied directly to the textarea.",
    },
    children: {
      control: false,
      description: "Reserved by `InputBase`; `TextArea` supplies its own input renderer.",
    },
  },
  render: (args) => <StatefulTextArea {...args} />,
} satisfies Meta<typeof TextArea>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const Required: Story = {
  args: {
    defaultValue: "",
    required: true,
    invalidHint: "A description is required.",
  },
  parameters: {
    docs: { description: { story: "Leave the field empty and blur it to display its required validation state." } },
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
  },
};
