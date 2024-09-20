import { TopPanel } from "./TopPanel";

export default () => {
  return (
    <>
      <p>TopPanel with icon and help link:</p>
      <TopPanel title="TopPanel with icon and links" icon="fa-filter" helpUrl="index.html">
        stuff
      </TopPanel>
    </>
  );
};
