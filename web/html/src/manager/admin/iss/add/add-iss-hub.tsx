import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";

const AddIssHub = () => {
  const [hub] = useState();

  let componentContent = (
    <div></div>
  );

  return componentContent;
};

export default hot(withPageWrapper(AddIssHub));
