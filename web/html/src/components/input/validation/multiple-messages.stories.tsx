import { useState } from "react";

import { Form, Text } from "components/input";

export default () => {
  const [model, setModel] = useState({ foo: "Hi" });

  const validator = (value: string) => {
    if (!value.toLowerCase().includes("i")) {
      return ["Must include the letter 'i'", "It's really gotta"];
    }
  };

  return (
    <Form model={model} onChange={setModel}>
      <p>You can return multiple validation errors:</p>
      <Text name="foo" validate={[validator]} />
    </Form>
  );
};
