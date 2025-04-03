import { Messages } from "./messages";

export default () => {
  return (
    <>
      <p>Messages can have different types:</p>
      <Messages
        items={[
          { severity: "error", text: "This is an example an error message." },
          { severity: "warning", text: "This is an example of a warning message." },
          { severity: "success", text: "This is an example of a success message." },
          { severity: "info", text: "This is an example of an info message." },
        ]}
      />
    </>
  );
};
