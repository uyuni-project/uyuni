import { StoryRow, StripedStorySection } from "manager/storybook/layout";
import * as React from "react";

import { StepsProgressBar } from "./steps-progress-bar";
import { Form } from "./input";
import { Text } from "./input";

export default () => {

  const detailsForm = [
    <Form
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Text
        name="name"
        label={t("Name")}
        required
        invalidHint={t("Minimum 2 characters")}
        labelClass="col-md-3"
        divClass="col-md-6"
        validators={[(value) => value.length > 2]}
      />
    </Form >
  ]

  const steps = [
    {
      title: "Details",
      content: detailsForm,
      validate: () => true,
    },
    { title: "Namespaces & Permissions", content: <p>Now you're on step 2</p>, validate: null, },
    { title: "Users", content: <p>Final step reached!</p> }
  ]
  return (
    <StepsProgressBar steps={steps}></StepsProgressBar>
  );
};
