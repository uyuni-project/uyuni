import * as React from "react";
import { ToastContainer, toast, cssTransition } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "./toastr.css";

type OptionalParams = {
  autoHide: boolean;
  containerId?: string;
};

type MessagesContainerProps = {
  containerId?: string;
};

const FadeTransition = cssTransition({
  enter: "toast-enter",
  exit: "toast-exit",
  duration: 0,
});

function show(message: React.ReactNode, notify: (arg0: React.ReactNode) => void) {
  if (Array.isArray(message)) {
    const combinedMessages = message.reduce((acc: React.ReactNodeArray, val) => acc.concat(val), []);
    for (const msg of combinedMessages) {
      notify(msg);
    }
  } else {
    notify(message);
  }
}

/** Parse `optionalParams.autoHide` into a valid configuration value */
function parseAutoHide(input: boolean) {
  // react-toastify accepts either a number or false, true is not a valid value, undefined results in using the default duration
  return input === true ? undefined : false;
}

export function showSuccessToastr(message: React.ReactNode, optionalParams: OptionalParams = { autoHide: true }) {
  const notify = (msg) =>
    toast.success(msg, {
      autoClose: parseAutoHide(optionalParams.autoHide),
      containerId: optionalParams.containerId,
    });
  show(message, notify);
}

export function showWarningToastr(message: React.ReactNode, optionalParams: OptionalParams = { autoHide: true }) {
  const notify = (msg) =>
    toast.warning(msg, {
      autoClose: parseAutoHide(optionalParams.autoHide),
      containerId: optionalParams.containerId,
    });
  show(message, notify);
}

export function showErrorToastr(message: React.ReactNode | Error, optionalParams: OptionalParams = { autoHide: true }) {
  const notify = (msg) =>
    toast.error(msg, {
      autoClose: parseAutoHide(optionalParams.autoHide),
      containerId: optionalParams.containerId,
    });

  if (message instanceof Error) {
    const msg = (message as Error).toString();
    notify(msg);
  } else {
    show(message, notify);
  }
}

export function showInfoToastr(message: React.ReactNode, optionalParams: OptionalParams = { autoHide: true }) {
  const notify = (msg) =>
    toast.info(msg, {
      autoClose: parseAutoHide(optionalParams.autoHide),
      containerId: optionalParams.containerId,
    });
  show(message, notify);
}

export const MessagesContainer = (props: MessagesContainerProps) => {
  return (
    <ToastContainer
      containerId={props.containerId}
      enableMultiContainer={true}
      position="top-center"
      autoClose={6000}
      hideProgressBar={true}
      closeOnClick={true}
      pauseOnHover={true}
      draggable={false}
      closeButton={false}
      transition={FadeTransition}
    />
  );
};
