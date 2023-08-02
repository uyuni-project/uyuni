import { Select } from "components/input";

const operators = [
  { label: t("lower"), value: "<" },
  { label: t("lower or equal"), value: "<=" },
  { label: t("equals"), value: "=" },
  { label: t("greater or equal"), value: ">=" },
  { label: t("greater"), value: ">" },
];

export const NumericSearchField = () => {
  return (
    <>
      <Select name="matcher" divClass="col-md-4" placeholder="Matcher" options={operators} />
    </>
  );
};
