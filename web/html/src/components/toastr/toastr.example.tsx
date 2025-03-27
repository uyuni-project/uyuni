import * as React from "react";

import { MessagesContainer, showSuccessToastr } from "./toastr";

export default () => {
  return (
    <>
      <MessagesContainer />
      <button onClick={() => showSuccessToastr("Great success")}>showSuccessToastr</button>
    </>
  );
};
