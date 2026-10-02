import { useState } from "react";

import { type MessageType, Messages } from "./messages";

export default () => {
  const [messages, setMessages] = useState<MessageType[]>([
    { severity: "error", text: "This is an example of an error message." },
    { severity: "warning", text: "This is an example of a warning message." },
    { severity: "success", text: "This is an example of a success message." },
    { severity: "info", text: "This is an example of an info message." },
  ]);

  return (
    <>
      <p>Messages can have different types:</p>
      <Messages
        items={messages}
        onClose={(index) => {
          setMessages((current) => current.filter((_, i) => i !== index));
        }}
      />
    </>
  );
};
