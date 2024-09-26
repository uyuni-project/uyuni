import { useState } from "react";

import { Form, Text } from "components/input";

const timeout = (ms = 0) => new Promise<void>((resolve) => window.setTimeout(() => resolve(), ms));

export default () => {
  const [model, setModel] = useState({ foo: "Foo" });

  const asyncValidator = async (value: string) => {
    await timeout(300);

    if (!value.toLowerCase().includes("o")) {
      return "Must include the letter 'o'";
    }
  };

  return (
    <Form model={model} onChange={setModel}>
      <p>Async validation with debounce:</p>
      <Text name="foo" validate={[asyncValidator]} debounceValidate={500} />
    </Form>
  );
};
