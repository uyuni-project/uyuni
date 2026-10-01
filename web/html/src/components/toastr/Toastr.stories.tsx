import type { Meta, StoryObj } from "@storybook/react-webpack5";
import type { ReactNode } from "react";

import { StoryRow, StorySection } from "manager/storybook/layout";

import { Button } from "components/buttons";

import { MessagesContainer, showErrorToastr, showInfoToastr, showSuccessToastr, showWarningToastr } from "./toastr";

/**
 * `MessagesContainer` is mounted with `enableMultiContainer`, so a container only accepts toasts whose
 * `containerId` matches its own. Every story uses its own id, otherwise the autodocs page — which mounts
 * all the stories at once — would show each toast in all of the containers on the page.
 */
const ToastrStory = ({ containerId, children }: { containerId: string; children: ReactNode }) => (
  <StorySection>
    <MessagesContainer containerId={containerId} />
    <StoryRow>{children}</StoryRow>
  </StorySection>
);

const meta = {
  title: "Components/Toastr",
  component: MessagesContainer,
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "Transient notifications built on `react-toastify`. Unlike `Messages`, these are triggered imperatively: mount a single `MessagesContainer` somewhere in the page and call `showInfoToastr`, `showSuccessToastr`, `showWarningToastr`, or `showErrorToastr` from anywhere. Toasts auto-close after 6 seconds by default and can be dismissed by clicking them.",
      },
    },
  },
} satisfies Meta<typeof MessagesContainer>;

export default meta;

const SEVERITIES_CONTAINER = "storybook-toastr-severities";
const AUTO_HIDE_CONTAINER = "storybook-toastr-auto-hide";
const MULTIPLE_CONTAINER = "storybook-toastr-multiple";
const ERROR_CONTAINER = "storybook-toastr-error";
const PLAYGROUND_CONTAINER = "storybook-toastr-playground";

type PlaygroundArgs = {
  severity: "info" | "success" | "warning" | "error";
  message: string;
  autoHide: boolean;
};

const playgroundHandlers = {
  info: showInfoToastr,
  success: showSuccessToastr,
  warning: showWarningToastr,
  error: showErrorToastr,
};

export const Playground: StoryObj<PlaygroundArgs> = {
  args: {
    severity: "success",
    message: "The system was registered.",
    autoHide: true,
  },
  argTypes: {
    severity: {
      control: "select",
      options: ["info", "success", "warning", "error"],
    },
    message: { control: "text" },
    autoHide: { control: "boolean" },
  },
  parameters: {
    docs: {
      description: {
        story: "Use the controls to pick the severity, edit the text, and decide whether the toast auto-closes.",
      },
      source: {
        type: "code",
        code: `<Button
          className="btn-primary"
          text="Show toastr"
          handler={() => playgroundHandlers[severity](message, { autoHide, containerId: PLAYGROUND_CONTAINER })}
        />`,
      },
    },
  },
  render: ({ severity, message, autoHide }) => (
    <ToastrStory containerId={PLAYGROUND_CONTAINER}>
      <Button
        className="btn-primary"
        text="Show toastr"
        handler={() => playgroundHandlers[severity](message, { autoHide, containerId: PLAYGROUND_CONTAINER })}
      />
    </ToastrStory>
  ),
};

export const Severities: StoryObj = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Use the severity that matches the outcome you are reporting.",
      },
      source: {
        type: "code",
        code: `<Button
        className="btn-default"
        text="Info"
        handler={() =>
          showInfoToastr("The channel is still synchronizing.", { autoHide: true, containerId: SEVERITIES_CONTAINER })
        }
      />
      <Button
        className="btn-default"
        text="Success"
        handler={() =>
          showSuccessToastr("The system was registered.", { autoHide: true, containerId: SEVERITIES_CONTAINER })
        }
      />
      <Button
        className="btn-default"
        text="Warning"
        handler={() =>
          showWarningToastr("Some channels are not synchronized yet.", {
            autoHide: true,
            containerId: SEVERITIES_CONTAINER,
          })
        }
      />
      <Button
        className="btn-default"
        text="Error"
        handler={() =>
          showErrorToastr("The package could not be installed.", { autoHide: true, containerId: SEVERITIES_CONTAINER })
        }
      />`,
      },
    },
  },
  render: () => (
    <ToastrStory containerId={SEVERITIES_CONTAINER}>
      <Button
        className="btn-default"
        text="Info"
        handler={() =>
          showInfoToastr("The channel is still synchronizing.", { autoHide: true, containerId: SEVERITIES_CONTAINER })
        }
      />
      <Button
        className="btn-default"
        text="Success"
        handler={() =>
          showSuccessToastr("The system was registered.", { autoHide: true, containerId: SEVERITIES_CONTAINER })
        }
      />
      <Button
        className="btn-default"
        text="Warning"
        handler={() =>
          showWarningToastr("Some channels are not synchronized yet.", {
            autoHide: true,
            containerId: SEVERITIES_CONTAINER,
          })
        }
      />
      <Button
        className="btn-default"
        text="Error"
        handler={() =>
          showErrorToastr("The package could not be installed.", { autoHide: true, containerId: SEVERITIES_CONTAINER })
        }
      />
    </ToastrStory>
  ),
};

export const AutoHide: StoryObj = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story:
          "`autoHide` defaults to true, which closes the toast after 6 seconds. Set it to false for messages the user has to acknowledge — those stay until clicked, since the container renders no close button.",
      },
      source: {
        type: "code",
        code: `<Button
        className="btn-default"
        text="Auto-hides after 6s"
        handler={() =>
          showInfoToastr("This toast closes on its own.", { autoHide: true, containerId: AUTO_HIDE_CONTAINER })
        }
      />
      <Button
        className="btn-default"
        text="Stays until clicked"
        handler={() =>
          showErrorToastr("This toast stays until you click it.", { autoHide: false, containerId: AUTO_HIDE_CONTAINER })
        }
      />`,
      },
    },
  },
  render: () => (
    <ToastrStory containerId={AUTO_HIDE_CONTAINER}>
      <Button
        className="btn-default"
        text="Auto-hides after 6s"
        handler={() =>
          showInfoToastr("This toast closes on its own.", { autoHide: true, containerId: AUTO_HIDE_CONTAINER })
        }
      />
      <Button
        className="btn-default"
        text="Stays until clicked"
        handler={() =>
          showErrorToastr("This toast stays until you click it.", { autoHide: false, containerId: AUTO_HIDE_CONTAINER })
        }
      />
    </ToastrStory>
  ),
};

export const MultipleMessages: StoryObj = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "Passing an array flattens it and raises one toast per entry, stacked in the container.",
      },
      source: {
        type: "code",
        code: `<Button
        className="btn-default"
        text="Show three toastrs"
        handler={() =>
          showWarningToastr(["The name is required.", "The port must be a number.", "The certificate has expired."], {
            autoHide: false,
            containerId: MULTIPLE_CONTAINER,
          })
        }
      />`,
      },
    },
  },
  render: () => (
    <ToastrStory containerId={MULTIPLE_CONTAINER}>
      <Button
        className="btn-default"
        text="Show three toastrs"
        handler={() =>
          showWarningToastr(["The name is required.", "The port must be a number.", "The certificate has expired."], {
            autoHide: false,
            containerId: MULTIPLE_CONTAINER,
          })
        }
      />
    </ToastrStory>
  ),
};

export const ErrorObject: StoryObj = {
  parameters: {
    controls: { disable: true },
    docs: {
      description: {
        story: "`showErrorToastr` also accepts an `Error`, which it renders through `toString()`.",
      },
    },
  },
  render: () => (
    <ToastrStory containerId={ERROR_CONTAINER}>
      <Button
        className="btn-default"
        text="Show an Error"
        handler={() =>
          showErrorToastr(new Error("Request failed with status 500"), {
            autoHide: false,
            containerId: ERROR_CONTAINER,
          })
        }
      />
    </ToastrStory>
  ),
};
