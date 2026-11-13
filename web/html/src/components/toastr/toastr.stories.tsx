/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { type ReactNode, useState } from "react";

import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Button } from "components/buttons";

import { MessagesContainer, showErrorToastr, showInfoToastr, showSuccessToastr, showWarningToastr } from "./toastr";

let nextContainerId = 0;

const ToastrExample = ({ children }: { children: (containerId: string) => ReactNode }) => {
  // Autodocs can mount multiple instances of the same story together.
  const [containerId] = useState(() => `storybook-toastr-${++nextContainerId}`);

  return (
    <div>
      <MessagesContainer containerId={containerId} />
      {children(containerId)}
    </div>
  );
};

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
      control: false,
      description: "Each mounted example uses its own container ID to isolate notifications.",
    },
  },
} satisfies Meta<typeof MessagesContainer>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  render: () => (
    <ToastrExample>
      {(containerId) => (
        <>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            <Button
              className="btn-default"
              text="Show Info"
              icon="fa-info-circle"
              handler={() => showInfoToastr("This is an informational toast message.", { autoHide: true, containerId })}
            />
            <Button
              className="btn-success"
              text="Show Success"
              icon="fa-check"
              handler={() => showSuccessToastr("Operation completed successfully!", { autoHide: true, containerId })}
            />
            <Button
              className="btn-warning"
              text="Show Warning"
              icon="fa-exclamation-triangle"
              handler={() =>
                showWarningToastr("Warning: This action may have side effects.", { autoHide: true, containerId })
              }
            />
            <Button
              className="btn-danger"
              text="Show Error"
              icon="fa-times-circle"
              handler={() =>
                showErrorToastr("An error occurred while processing your request.", { autoHide: true, containerId })
              }
            />
          </div>
          <p style={{ marginTop: "2rem" }}>
            <em>Click the buttons above to see toast notifications appear at the top of the screen.</em>
          </p>
        </>
      )}
    </ToastrExample>
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
    <ToastrExample>
      {(containerId) => (
        <div style={{ display: "flex", gap: "10px", flexDirection: "column", maxWidth: "300px" }}>
          <Button
            className="btn-default"
            text="Auto-hide (default)"
            handler={() =>
              showSuccessToastr("This toast will auto-hide after 6 seconds.", { autoHide: true, containerId })
            }
          />
          <Button
            className="btn-default"
            text="Stay visible"
            handler={() => showSuccessToastr("This toast stays until you click it.", { autoHide: false, containerId })}
          />
        </div>
      )}
    </ToastrExample>
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
    <ToastrExample>
      {(containerId) => (
        <Button
          className="btn-default"
          text="Show Multiple Toasts"
          handler={() => {
            showSuccessToastr("First operation completed", { autoHide: true, containerId });
            showInfoToastr("Processing next step...", { autoHide: true, containerId });
            showSuccessToastr("All operations completed successfully!", { autoHide: true, containerId });
          }}
        />
      )}
    </ToastrExample>
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
    <ToastrExample>
      {(containerId) => (
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
              { autoHide: false, containerId }
            )
          }
        />
      )}
    </ToastrExample>
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
    <ToastrExample>
      {(containerId) => (
        <Button
          className="btn-danger"
          text="Show Error Object"
          handler={() => {
            const error = new Error("Network connection failed");
            showErrorToastr(error, { autoHide: true, containerId });
          }}
        />
      )}
    </ToastrExample>
  ),
  parameters: {
    docs: {
      description: {
        story: "`showErrorToastr` can accept an Error object and will automatically convert it to a string.",
      },
    },
  },
};
