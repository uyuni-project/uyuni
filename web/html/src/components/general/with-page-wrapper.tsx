import * as React from "react";
import DefaultJsError from "manager/errors/default-js-error";

class State {
  hasError = false;
}

type Props = {
  children: React.ReactNode;
};

class ErrorBoundary extends React.Component<Props, State> {
  state = new State();

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    this.setState({ hasError: true });
    // Log error to server
    Loggerhead.error(`
      stack: ${error.stack}
      componentStack: ${info.componentStack}
    `);
  }

  render() {
    if (this.state.hasError) {
      return <DefaultJsError />;
    }
    return this.props.children;
  }
}

const PageWrapper = ErrorBoundary;

// The types could be improved here, possibly similar to how React.ElementRef handles it
function withPageWrapper<WrapperProps>(
  Component: React.ComponentClass<WrapperProps> | ((props: WrapperProps) => JSX.Element)
) {
  return function WrapperComponent(props: WrapperProps) {
    return (
      <PageWrapper>
        <Component {...props} />
      </PageWrapper>
    );
  };
}

export default withPageWrapper;
