import { components as WrapperComponents } from "react-select";

import { ClearIndicator } from "./ClearIndicator";

const TestIdContainer = function ({
  children,
  ...props
}: React.ComponentProps<typeof WrapperComponents.SelectContainer>) {
  const innerProps = Object.assign(
    {
      "data-testid": props.selectProps["data-testid"],
    },
    props.innerProps
  );
  return (
    <WrapperComponents.SelectContainer {...props} innerProps={innerProps}>
      {children}
    </WrapperComponents.SelectContainer>
  );
};

export default function withCustomComponents(testId: string | undefined, fallbackName: string | undefined) {
  // The name-based fallback is used to keep compatibility with existing forms that use the `name` prop for binding to tests
  const testClassName = testId || fallbackName;
  const classNamePrefix = testClassName ? `data-testid-${testClassName}-child` : "react-select";

  return {
    classNamePrefix,
    components: testId
      ? {
          SelectContainer: TestIdContainer,
          ClearIndicator,
        }
      : { ClearIndicator },
    "data-testid": testId,
  };
}
