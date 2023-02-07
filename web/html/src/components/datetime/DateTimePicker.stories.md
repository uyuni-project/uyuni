This demo is mostly useful for debugging updates to the picker and ensuring all the values still line up correctly.

```jsx
import { useState } from "react";

import moment from "moment";

import { localizedMoment } from "utils";

import { DateTimePicker } from "./DateTimePicker";

const [value, setValue] = useState(localizedMoment());

const legacyId = "legacy";

<div>
  <p>
    user time zone: {localizedMoment.userTimeZone.displayValue} (
    {localizedMoment(value).tz(localizedMoment.userTimeZone).format("Z")})
  </p>
  <p>
    server time zone: {localizedMoment.serverTimeZone.displayValue} (
    {localizedMoment(value).tz(localizedMoment.serverTimeZone).format("Z")})
  </p>
  <p>
    user time:
    {localizedMoment(value).tz(localizedMoment.userTimeZone).toISOString(true)}
  </p>
  <p>
    server time:
    {localizedMoment(value).tz(localizedMoment.serverTimeZone).toISOString(true)}
  </p>
  <p>iso time: {value.toISOString()}</p>
  {/* eslint-disable-next-line local-rules/no-raw-date */}
  <p>browser time: {moment().toISOString(true)}</p>
  <p>legacy id: "{legacyId}"</p>
  <DateTimePicker value={value} onChange={(newValue) => setValue(newValue)} legacyId={legacyId} />
</div>
```
