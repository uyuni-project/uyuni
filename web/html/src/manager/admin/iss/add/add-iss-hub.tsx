import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";

const addHubAPI = (hubData) => {
  return fetch("/rhn/manager/api/iss/add/hub", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(hubData),
  }).then((response) => {
    if (!response.ok) {
      return response.json().then((errorData) => {
        throw errorData;
      });
    }
    return response.json();
  });
};

const AddIssHub = () => {
  const [hub] = useState();

  let componentContent = (
    <span>
      <h1>Add an Hub</h1>
    </span>
  );

  return componentContent;
};

export default hot(withPageWrapper(AddIssHub));
