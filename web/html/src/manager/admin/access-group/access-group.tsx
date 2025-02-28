import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useState } from "react";

import AccessGroupDetails from "./access-group-details";
import { AsyncButton } from "components/buttons";
import withPageWrapper from "components/general/with-page-wrapper";
import { TopPanel } from "components/panels/TopPanel";
import { showErrorToastr } from "components/toastr/toastr";

import { StepsProgressBar } from "components/steps-progress-bar";

const CreateAccessGroup = () => {

  const [accessGroupDetails, setAccessGroupDetails] = useState({
    properties: {
      name: "",
      label: "",
      description: "",
      org_id: ""
    },
    errors: { "test": "dsgysgd" },
  });
  const handleFormChange = (newProperties) => {
    console.log("Before Update:", accessGroupDetails.properties); // Logs previous state
    console.log("New Input:", newProperties); // Logs new input

    setAccessGroupDetails((prev) => {
      const updatedState = {
        ...prev,
        properties: { ...prev.properties, ...newProperties },
      };
      console.log("Updated State:", updatedState.properties); // Logs after merging
      return updatedState;
    });
  };

  const steps = [
    {
      title: "Details",
      content: <AccessGroupDetails properties={accessGroupDetails.properties}
        errors={accessGroupDetails.errors}
        onChange={handleFormChange} />,
      validate: () => true,
    },
    { title: "Namespaces & Permissions", content: <p>Now you're on step 2</p>, validate: null, },
    { title: "Users", content: <p>Final step reached!</p> }
  ]
  return (
    <TopPanel
      title={t("Access Group")}
    >
      <StepsProgressBar steps={steps}></StepsProgressBar>
    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateAccessGroup));
