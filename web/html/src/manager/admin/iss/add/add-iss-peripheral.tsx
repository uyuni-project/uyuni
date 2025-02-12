import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import withPageWrapper from "components/general/with-page-wrapper";

const addPeripheralAPI = (peripheralData) => {
  return fetch("/rhn/manager/api/iss/add/peripheral", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(peripheralData),
  }).then((response) => {
    if (!response.ok) {
      return response.json().then((errorData) => {
        throw errorData;
      });
    }
    return response.json();
  });
};

const AddIssPeripheral = () => {
  const [peripheral] = useState();

  let componentContent = (
    <span>
      <h1>Add a Peripheral</h1>
    </span>
  );

  return componentContent;
};

export default hot(withPageWrapper(AddIssPeripheral));
