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

export function showSuccessToastr(message: string|React.Node, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = () => toast.success(message, {
    autoClose: optionalParams.autoHide
    });
  notify();
}

export function showWarningToastr(message: string|React.Node, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = () => toast.warning(message, {
    autoClose: optionalParams.autoHide
    });
  notify();
}

export function showErrorToastr(message: string|React.Node|Error, optionalParams: OptionalParams = {autoHide: true}) {
  if (message instanceof Error) {
    message = (message: Error).toString();
  }
  const notify = () => toast.error(message, {
    autoClose: optionalParams.autoHide
    });
  notify();
}

export function showInfoToastr(message: string|React.Node, optionalParams: OptionalParams = {autoHide: true}) {
  const notify = () => toast.info(message, {
    autoClose: optionalParams.autoHide,
    });
  notify();
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