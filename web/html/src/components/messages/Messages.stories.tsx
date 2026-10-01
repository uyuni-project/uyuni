import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StorySection } from "manager/storybook/layout";

import { Messages, Utils } from "./messages";

const meta = {
  title: "Components/Messages",
  component: Messages,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Renders one or more alert messages. Pass a single message object or an array through `items`. Use the `Messages.info/success/warning/error` helpers to build a single message, or `Utils.info/success/warning/error` to build an array out of one or several texts.",
      },
    },
  },
  args: {
    items: Utils.info("This is an example of an info message."),
    dismissible: false,
  },
  argTypes: {
    items: { control: "object" },
    dismissible: { control: "boolean" },
    onClose: { action: "closed" },
  },
} satisfies Meta<typeof Messages>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Use the controls to edit the `items` array and toggle the dismiss button.",
      },
    },
  },
};

export const Severities: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Use the severity that matches the nature of the message.",
      },
    },
  },
  render: () => (
    <StorySection>
      <Messages items={Messages.error("This is an example of an error message.")} />
      <Messages items={Messages.warning("This is an example of a warning message.")} />
      <Messages items={Messages.success("This is an example of a success message.")} />
      <Messages items={Messages.info("This is an example of an info message.")} />
    </StorySection>
  ),
};

export const MultipleMessages: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "An array of messages is rendered as separate alerts, one below the other.",
      },
    },
  },
  render: () => (
    <StorySection>
      <Messages
        items={[
          ...Utils.error("The package could not be installed."),
          ...Utils.warning("Some channels are not synchronized yet."),
          ...Utils.success("The system was registered."),
        ]}
      />
    </StorySection>
  ),
};

export const ListedMessages: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Pass an array of texts to a `Utils` helper with `listMultiple` set to true to group them into a single alert. The optional third argument adds a header above the list.",
      },
      // `listMultiple` builds a message whose `text` is a React element. The dynamic source serializer
      // walks object props with `stringify-object`, which recurses into the element internals instead of
      // stopping at it, and blows up with "Invalid string length". Give it a fixed snippet instead.
      source: {
        type: "code",
        code: `<Messages
  items={Utils.error(
    ["The name is required.", "ss The port must be a number.", "The certificate has expired."],
    true,
    "The configuration could not be saved:"
  )}
/>`,
      },
    },
  },
  render: () => (
    <StorySection>
      <Messages
        items={Utils.error(
          ["The name is required.", "The port must be a number.", "The certificate has expired."],
          true,
          "The configuration could not be saved:"
        )}
      />
    </StorySection>
  ),
};

export const Dismissible: Story = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "Set `dismissible` to show a close button on every message. The component hides the dismissed message on its own, so `onClose` is only needed when the parent has to react to it.",
      },
    },
  },
  render: () => (
    <StorySection>
      <Messages
        dismissible
        items={[
          ...Utils.error("This is an example of an error message."),
          ...Utils.warning("This is an example of a warning message."),
          ...Utils.success("This is an example of a success message."),
          ...Utils.info("This is an example of an info message."),
        ]}
      />
    </StorySection>
  ),
};
