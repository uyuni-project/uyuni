import { Loading } from "./Loading";

export default () => {
  return (
    <>
      <p>Loading indicator with text:</p>
      <Loading text={t("Loading text")} />
    </>
  );
};
