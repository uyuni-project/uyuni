import { useState } from "react";

import { Form, Text } from "components/input";

const timeout = (ms = 0) => new Promise<void>((resolve) => window.setTimeout(() => resolve(), ms));

export default () => {
  const [model, setModel] = useState({ foo: "Foo" });

  const asyncValidator = async (value: string) => {
    await timeout(300);

    if (value.length < 3) {
      return "Must be at least 3 characters long";
    }
  };

  return (
    <Form model={model} onChange={(newModel) => setModel(newModel)}>
      <p>Inputs support async validation with debounce:</p>
      <Text name="foo" validators={[asyncValidator]} debounceValidate={500} />
    </Form>
  );
};
