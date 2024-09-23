import { useState } from "react";

import { Form, Text } from "components/input";

export default () => {
  const [model, setModel] = useState({ foo: "Foo" });

  const validator = (value: string) => {
    if (!value.toLowerCase().includes("o")) {
      return ["Must include the letter 'o'", "It's really gotta"];
    }
  };

  return (
    <Form model={model} onChange={(newModel) => setModel(newModel)}>
      <p>You can return multiple validation errors:</p>
      <Text name="foo" validate={[validator]} />
    </Form>
  );
};
