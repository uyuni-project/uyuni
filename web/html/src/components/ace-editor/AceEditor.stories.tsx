import { useEffect, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { AceEditor } from "./index";

type AceEditorProps = React.ComponentProps<typeof AceEditor>;

const sampleCode = `const greeting = "Hello from Uyuni";

export function Example() {
  return <p>{greeting}</p>;
}`;

const StatefulAceEditor = (props: AceEditorProps) => {
  const [content, setContent] = useState(typeof props.content === "string" ? props.content : "");

  useEffect(() => {
    setContent(typeof props.content === "string" ? props.content : "");
  }, [props.content]);

  return (
    <AceEditor
      {...props}
      content={content}
      onChange={(value) => {
        setContent(value);
        props.onChange?.(value);
      }}
    />
  );
};

const meta = {
  title: "Components/Inputs/AceEditor",
  component: AceEditor,
  parameters: {
    docs: {
      description: {
        component:
          "Code editor powered by Ace. Use it for scripts and configuration files that need syntax highlighting, editable content, or a read-only presentation.",
      },
    },
  },
  args: {
    mode: "jsx",
    content: sampleCode,
    minLines: 10,
    maxLines: 20,
    readOnly: false,
    onChange: action("content changed"),
  },
  argTypes: {
    mode: {
      control: "select",
      options: ["jsx", "json", "text"],
      description: "Ace syntax mode. Storybook bundles the modes used by these examples.",
    },
    content: {
      control: "text",
      description: "Editor content.",
      table: { type: { summary: "string" } },
    },
    minLines: {
      control: { type: "number", min: 1 },
      description: "Minimum number of visible editor lines.",
    },
    maxLines: {
      control: { type: "number", min: 1 },
      description: "Maximum number of visible editor lines.",
    },
    readOnly: {
      control: "boolean",
      description: "Prevents editing while retaining syntax highlighting and selection.",
    },
    onChange: {
      action: "content changed",
      description: "Called with the complete editor content after a change.",
    },
  },
  render: (args) => <StatefulAceEditor {...args} />,
} satisfies Meta<typeof AceEditor>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};

export const ReadOnly: Story = {
  args: {
    readOnly: true,
  },
};

export const JsonConfiguration: Story = {
  args: {
    mode: "json",
    content: JSON.stringify({ server: "uyuni.example.com", enabled: true, channels: ["base", "updates"] }, null, 2),
  },
};
