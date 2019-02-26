// @flow
// globals Loggerhead
import * as React from 'react';
import DefaultError500 from '../../manager/errors/default-error-500';
import { hot } from 'react-hot-loader';

declare var Loggerhead: any;

class ErrorBoundary extends React.Component<{children: React.Node}, {hasError: boolean}> {
  constructor() {
    super();
    this.state = { hasError: false };
  }

  componentDidCatch(error, info) {
    this.setState({ hasError: true });
    // Log error to server
    Loggerhead.error(`
      stack: ${error.stack}
      componentStack: ${info.componentStack}
    `);
  }

  render() {
    if (this.state.hasError) {
      return <DefaultError500/>;
    }
    return this.props.children;
  }
}

const PageWrapper = hot(module)(ErrorBoundary)

function withPageWrapper<Config: {}>(
  Component: React.AbstractComponent<Config>
): React.AbstractComponent<Config> {
  return function WrapperComponent(
    props: Config,
  ) {
    return (
      <PageWrapper>
        <Component {...props} />
      </PageWrapper>
    )
  };
}

export default withPageWrapper;
