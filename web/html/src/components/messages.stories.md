Messages can have different types:

```jsx
import { Messages } from "components/messages";

<Messages
  items={[
    { severity: "error", text: "This is an example an error message." },
    { severity: "warning", text: "This is an example of a warning message." },
    { severity: "success", text: "This is an example of a success message." },
    { severity: "info", text: "This is an example of an info message." },
  ]}
/>
```

Creating messages using helper utilities:

```jsx
import { Messages, Utils } from "components/messages";

<Messages items={Utils.success("My success message created using the `Utils.success()` method.")} />
```
