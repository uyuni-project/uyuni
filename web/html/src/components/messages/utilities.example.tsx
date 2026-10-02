import { Messages, Utils } from "./messages";

export default () => {
  return (
    <>
      <p>Creating messages using helper utilities:</p>
      <Messages dismissible items={Utils.success("My success message created using the `Utils.success()` method.")} />
    </>
  );
};
