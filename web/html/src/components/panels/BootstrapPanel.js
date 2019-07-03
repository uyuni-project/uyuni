const React = require('react');

const { Panel } = require('./Panel');

type Props = {
  title?: string,
  icon?: string,
  header?: string,
  footer?: string,
  children: React.Node,
};

function BootstrapPanel(props: Props) {
  return (
    <Panel
      headingLevel="h2"
      title={props.title}
      icon={props.icon}
      header={props.header}
      footer={props.footer}
      buttons={props.buttons}
    >
      {props.children}
    </Panel>
  );
}

BootstrapPanel.defaultProps = {
  title: undefined,
  icon: undefined,
  header: undefined,
  footer: undefined,
};

module.exports = {
  BootstrapPanel,
};
