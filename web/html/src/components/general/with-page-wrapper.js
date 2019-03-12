// @flow
// globals Loggerhead
import * as React from 'react';
import DefaultJsError from '../../manager/errors/default-js-error';
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
      return <DefaultJsError/>;
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
