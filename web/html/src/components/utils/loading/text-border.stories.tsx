import { Loading } from "./Loading";

export default () => {
  return (
    <>
      <p>Loading indicator with text and borders:</p>
      <Loading text={t("Loading text")} withBorders />
    </>
  );
};
