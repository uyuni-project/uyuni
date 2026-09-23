import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Button } from "components/buttons";

import { MessagesContainer, showErrorToastr, showInfoToastr, showSuccessToastr, showWarningToastr } from "./toastr";

const meta = {
  title: "Components/Feedback/Toastr",
  component: MessagesContainer,
  parameters: {
    docs: {
      description: {
        component:
          "Toast notification system powered by react-toastify. Displays temporary notification messages at the top-center of the screen. Supports info, success, warning, and error messages with auto-hide functionality. Use `MessagesContainer` to mount the toast container, then call show functions to display toasts.",
      },
    },
  },
  argTypes: {
    containerId: {
      control: "text",
      description: "Optional container ID for using multiple toast containers.",
      table: { type: { summary: "string" } },
    },
  },
} satisfies Meta<typeof MessagesContainer>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: () => (
    <div>
      <MessagesContainer />
      <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
        <Button
          className="btn-default"
          text="Show Info"
          icon="fa-info-circle"
          handler={() => showInfoToastr("This is an informational toast message.")}
        />
        <Button
          className="btn-success"
          text="Show Success"
          icon="fa-check"
          handler={() => showSuccessToastr("Operation completed successfully!")}
        />
        <Button
          className="btn-warning"
          text="Show Warning"
          icon="fa-exclamation-triangle"
          handler={() => showWarningToastr("Warning: This action may have side effects.")}
        />
        <Button
          className="btn-danger"
          text="Show Error"
          icon="fa-times-circle"
          handler={() => showErrorToastr("An error occurred while processing your request.")}
        />
      </div>
      <p style={{ marginTop: "2rem" }}>
        <em>Click the buttons above to see toast notifications appear at the top of the screen.</em>
      </p>
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story:
          "Interactive toast notifications. Click buttons to trigger different severity levels. Toasts auto-hide after 6 seconds by default.",
      },
    },
  },
};

export const AutoHide: Story = {
  render: () => (
    <div>
      <MessagesContainer />
      <div style={{ display: "flex", gap: "10px", flexDirection: "column", maxWidth: "300px" }}>
        <Button
          className="btn-default"
          text="Auto-hide (default)"
          handler={() => showSuccessToastr("This toast will auto-hide after 6 seconds.")}
        />
        <Button
          className="btn-default"
          text="Stay visible"
          handler={() => showSuccessToastr("This toast stays until you click it.", { autoHide: false })}
        />
      </div>
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story:
          "Control auto-hide behavior. By default, toasts auto-hide after 6 seconds. Pass `{ autoHide: false }` to keep them visible until clicked.",
      },
    },
  },
};

export const MultipleMessages: Story = {
  render: () => (
    <div>
      <MessagesContainer />
      <Button
        className="btn-default"
        text="Show Multiple Toasts"
        handler={() => {
          showSuccessToastr("First operation completed");
          setTimeout(() => showInfoToastr("Processing next step..."), 500);
          setTimeout(() => showSuccessToastr("All operations completed successfully!"), 1000);
        }}
      />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "Multiple toast messages can be displayed simultaneously. They stack vertically.",
      },
    },
  },
};

export const WithJSX: Story = {
  render: () => (
    <div>
      <MessagesContainer />
      <Button
        className="btn-danger"
        text="Show Error with Details"
        handler={() =>
          showErrorToastr(
            <div>
              <strong>Validation Failed</strong>
              <ul style={{ marginTop: "8px", marginBottom: 0, paddingLeft: "20px" }}>
                <li>Username is required</li>
                <li>Password must be at least 8 characters</li>
                <li>Email format is invalid</li>
              </ul>
            </div>,
            { autoHide: false }
          )
        }
      />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "Toast messages can contain JSX/React elements for rich formatting.",
      },
    },
  },
};

export const ErrorObject: Story = {
  render: () => (
    <div>
      <MessagesContainer />
      <Button
        className="btn-danger"
        text="Show Error Object"
        handler={() => {
          const error = new Error("Network connection failed");
          showErrorToastr(error);
        }}
      />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: "`showErrorToastr` can accept an Error object and will automatically convert it to a string.",
      },
    },
  },
};
