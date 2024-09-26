import { useState } from "react";

import { Form, Text, Validate } from "components/input";

export default () => {
  const [model, setModel] = useState({ foo: "Foo", bar: "3", tea: "Hi" });

  return (
    <Form model={model} onChange={setModel}>
      <p>There are numerous validation helpers:</p>
      <Text name="foo" validate={[Validate.matches(/^F/), Validate.minLength(3)]} />
      <Text name="bar" validate={Validate.isInt()} />
      <Text name="tea" validate={Validate.matches(/[a-z]/, "Must include a lowercase letter")} />
    </Form>
  );
};
