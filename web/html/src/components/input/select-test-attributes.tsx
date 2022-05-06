import { components as WrapperComponents } from "react-select";

const Input = function (props) {
  return <WrapperComponents.Input {...props} data-testid={props.selectProps["data-testid"]} />;
};

export default function withTestAttributes(testId: string | undefined) {
  return {
    components: testId
      ? {
          Input,
        }
      : undefined,
    "data-testid": testId,
  };
}
