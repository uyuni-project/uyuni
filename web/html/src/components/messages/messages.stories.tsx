import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { StoryRow, StripedStorySection } from "manager/storybook/layout";

import { Messages, MessageType } from "./messages";

const meta = {
  title: "Components/Feedback/Messages",
  component: Messages,
  parameters: {
    docs: {
      description: {
        component:
          "Component for displaying inline alert messages with different severity levels. Supports info, success, warning, and error messages. Can display single or multiple messages. Also provides utility methods (`Messages.info()`, `Messages.success()`, etc.) for creating message objects.",
      },
    },
  },
  args: {
    items: Messages.info("This is an informational message."),
  },
  argTypes: {
    items: {
      control: "object",
      description:
        "Message object(s) to display. Can be a single message or an array of messages. Each message has `severity` and `text` properties.",
      table: {
        type: {
          summary: "MessageType | MessageType[]",
        },
      },
    },
  },
} satisfies Meta<typeof Messages>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  parameters: {
    docs: {
      description: {
        story: "Interactive message display. Try changing the message text in the controls.",
      },
    },
  },
};

export const Info: Story = {
  args: {
    items: Messages.info("This is an informational message with helpful context."),
  },
  parameters: {
    docs: {
      description: {
        story: "Info message with blue styling for general information.",
      },
    },
  },
};

export const Success: Story = {
  args: {
    items: Messages.success("Operation completed successfully!"),
  },
  parameters: {
    docs: {
      description: {
        story: "Success message with green styling for successful operations.",
      },
    },
  },
};

export const Warning: Story = {
  args: {
    items: Messages.warning("This action may have unintended consequences. Please review before proceeding."),
  },
  parameters: {
    docs: {
      description: {
        story: "Warning message with yellow styling for cautionary information.",
      },
    },
  },
};

export const Error: Story = {
  args: {
    items: Messages.error("An error occurred while processing your request. Please try again."),
  },
  parameters: {
    docs: {
      description: {
        story: "Error message with red styling for errors and failures.",
      },
    },
  },
};

export const MultipleMessages: Story = {
  args: {
    items: [
      Messages.success("Configuration saved successfully."),
      Messages.warning("Some settings require a system restart to take effect."),
      Messages.info("Documentation has been updated with the latest changes."),
    ],
  },
  parameters: {
    docs: {
      description: {
        story: "Multiple messages displayed together, each with its own severity level.",
      },
    },
  },
};

export const WithJSX: Story = {
  args: {
    items: Messages.error(
      <div>
        <strong>Validation failed:</strong>
        <ul>
          <li>Username is required</li>
          <li>Password must be at least 8 characters</li>
          <li>Email format is invalid</li>
        </ul>
      </div>
    ),
  },
  parameters: {
    docs: {
      description: {
        story: "Messages can contain JSX/React elements for rich formatting.",
      },
    },
  },
};

export const AllSeverities: Story = {
  render: () => (
    <StripedStorySection>
      <StoryRow>
        <Messages items={Messages.info("Info: System update available")} />
      </StoryRow>
      <StoryRow>
        <Messages items={Messages.success("Success: Changes saved")} />
      </StoryRow>
      <StoryRow>
        <Messages items={Messages.warning("Warning: Disk space running low")} />
      </StoryRow>
      <StoryRow>
        <Messages items={Messages.error("Error: Connection failed")} />
      </StoryRow>
    </StripedStorySection>
  ),
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "All four severity levels displayed for comparison.",
      },
    },
  },
};

export const UsageExample: Story = {
  render: () => {
    const validationMessages: MessageType[] = [
      Messages.error("Required field 'Email' is missing."),
      Messages.warning("Password strength is weak."),
    ];

    return (
      <div>
        <h4>Form Validation</h4>
        <Messages items={validationMessages} />
        <div style={{ marginTop: "1rem", padding: "1rem", backgroundColor: "#f5f5f5", borderRadius: "4px" }}>
          <p>
            <strong>Code example:</strong>
          </p>
          <pre style={{ fontSize: "12px" }}>
            {`const messages = [
  Messages.error("Required field 'Email' is missing."),
  Messages.warning("Password strength is weak.")
];

<Messages items={messages} />`}
          </pre>
        </div>
      </div>
    );
  },
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Common usage pattern for form validation messages.",
      },
    },
  },
};
