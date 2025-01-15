import { useState } from "react";

import { StorySection } from "manager/storybook/layout";

import { localizedMoment } from "utils";

import { DateTimePicker } from "./DateTimePicker";
import { FromNow, fromNow } from "./FromNow";

export default () => {
  const [value, setValue] = useState(localizedMoment());

  return (
    <StorySection>
      <p>value:</p>
      <div>
        <DateTimePicker value={value} onChange={(newValue) => setValue(newValue)} />
      </div>

      <p>
        <code>fromNow</code> function
      </p>
      <p>
        <code>{fromNow(value)}</code>
      </p>

      <p>
        <code>FromNow</code> component with prop value
      </p>
      <p>
        <FromNow value={value} />
      </p>
    </StorySection>
  );
};
