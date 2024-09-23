import { useState } from "react";

import { Form, Text, Validate } from "components/input";

export default () => {
  const [model, setModel] = useState({ foo: "Foo", bar: "3" });

  return (
    <Form model={model} onChange={(newModel) => setModel(newModel)}>
      <p>There are numerous validation helpers:</p>
      <Text name="foo" validate={[Validate.all([Validate.matches(/^F/), Validate.minLength(3)])]} />
      <Text name="bar" validate={[Validate.isInt]} />
    </Form>
  );
};
