```jsx
import { useState } from "react";

import { localizedMoment } from "utils";

import { fromNow, FromNow } from "./FromNow";
import { DateTimePicker } from "./DateTimePicker";

const [value, setValue] = useState(localizedMoment());

<div>
  <p>value:</p>
    <p>
    <DateTimePicker value={value} onChange={(newValue) => setValue(newValue)} />
  </p>

  <p><code>fromNow</code> function</p>
  <p>
    <pre>{fromNow(value)}</pre>
  </p>

  <p><code>FromNow</code> component with prop value</p>
  <p>
    <FromNow value={value} />
  </p>

  <p><code>FromNow</code> component with child value</p>
  <p>
    <FromNow>{value}</FromNow>
  </p>
</div>
```
