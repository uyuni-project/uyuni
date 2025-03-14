import { BootstrapPanel } from "./BootstrapPanel";

export default () => {
  return (
    <>
      <p>BootstrapPanel with title:</p>
      <BootstrapPanel title="Bootstrap title" footer="testing">
        stuff
      </BootstrapPanel>
    </>
  );
};
