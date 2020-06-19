// @flow
import * as React from 'react';
import {ToastContainer, toast, cssTransition} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import  "./toastr.css";

type OptionalParams = {
  autoHide: boolean
}

type MessagesContainerProps = {
}

const FadeTransition = cssTransition({
  enter: 'toast-enter',
  exit: 'toast-exit',
  duration: 0
});

function show(message: string | React.Node | Array<string | React.Node>, notify: (string | React.Node) => void) {
  if (Array.isArray(message)) {
    message = message.reduce((acc, val) => acc.concat(val), []);
    for (const msg of message) {
      notify(msg);
    }
  } else {
    notify(message);
  }
}

export function showSuccessToastr(message: string | React.Node, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = (msg) => toast.success(msg, {
    autoClose: optionalParams.autoHide
    });
  show(message, notify)
}

export function showWarningToastr(message: string|React.Node, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = (msg) => toast.warning(msg, {
    autoClose: optionalParams.autoHide
    });

  show(message, notify)
}

export function showErrorToastr(message: string | React.Node | Error | Array<string | React.Node>, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = (msg) => toast.error(msg, {
    autoClose: optionalParams.autoHide
    });

  if (message instanceof Error) {
    const msg = (message: Error).toString();
    notify(msg)
  } else {
    show(message, notify)
  }
}

export function showInfoToastr(message: string|React.Node, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = (msg) => toast.info(msg, {
    autoClose: optionalParams.autoHide,
    });
  show(message, notify)
}

export const MessagesContainer = (props: MessagesContainerProps) => {
  return <ToastContainer
    position="top-center"
    autoClose={6000}
    hideProgressBar={true}
    closeOnClick={true}
    pauseOnHover={true}
    draggable={false}
    progress="undefined"
    closeButton={false}
    transition={FadeTransition}
  />
}
