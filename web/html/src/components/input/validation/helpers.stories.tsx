import { useState } from "react";

import { Form, Text, Validation } from "components/input";

export default () => {
  const [model, setModel] = useState({ foo: "Foo", bar: "3", tea: "Hi" });

  return (
    <Form model={model} onChange={setModel}>
      <p>There are numerous validation helpers:</p>
      <Text name="foo" validate={[Validation.matches(/^F/), Validation.minLength(3)]} />
      <Text name="bar" validate={Validation.isInt()} />
      <Text name="tea" validate={Validation.matches(/[a-z]/, "Must include a lowercase letter")} />
    </Form>
  );
};
