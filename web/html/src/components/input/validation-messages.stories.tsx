import { useState } from "react";

import { Form, Text } from "components/input";

export default () => {
  const [model, setModel] = useState({ foo: "Foo" });

  const validator = (value: string) => {
    if (value.length < 3) {
      return ["Must be at least 3 characters long", "It's really gotta"];
    }
  };

  return (
    <Form model={model} onChange={(newModel) => setModel(newModel)}>
      <p>You can return multiple validation errors:</p>
      <Text name="foo" validators={[validator]} />
    </Form>
  );
};
