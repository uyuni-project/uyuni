```jsx
import * as React from "react";
import { showSuccessToastr } from "./toastr";

<React.Fragment>
  <MessagesContainer />
  <button onClick={() => showSuccessToastr("Great success")}>showSuccessToastr</button>
</React.Fragment>
```
