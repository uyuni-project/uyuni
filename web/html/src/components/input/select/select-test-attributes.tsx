import { components as WrapperComponents } from "react-select";

const TestIdContainer = function ({ children, ...props }) {
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

export default function withTestAttributes(testId: string | undefined, fallbackName: string | undefined) {
  // The name-based fallback is used to keep compatibility with existing forms that use the `name` prop for binding to tests
  const testClassName = testId || fallbackName || undefined;
  const classNamePrefix = testClassName ? `data-testid-${testClassName}-child` : undefined;

  return {
    classNamePrefix,
    components: testId
      ? {
          SelectContainer: TestIdContainer,
        }
      : undefined,
    "data-testid": testId,
  };
}
