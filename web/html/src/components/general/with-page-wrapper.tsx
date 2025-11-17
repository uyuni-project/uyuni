import { Component } from "react";

import DefaultJsError from "manager/errors/default-js-error";

class State {
  hasError = false;
}

type Props = {
  children: React.ReactNode;
};

class ErrorBoundary extends Component<Props, State> {
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

function withPageWrapper<WrapperProps extends Record<string, unknown>>(
  Component: React.ComponentType<WrapperProps>
): React.FC<WrapperProps> {
  return function WrapperComponent(props: WrapperProps) {
    return (
      <PageWrapper>
        <Component {...props} />
      </PageWrapper>
    );
  };
}

export default withPageWrapper;
