const React = require('react');

type Props = {
  headingLevel: string,
  title?: string,
  icon?: string,
  children: React.Node,
};

function Panel(props: Props) {
  const { headingLevel } = props;
  return (
    <div className="panel panel-default">
      {props.title
        && (
        <div className="panel-heading">
          <headingLevel>
            { props.icon && <i className={`fa ${props.icon}`} /> }
            { props.title }
          </headingLevel>
        </div>)
      }
      <div className="panel-body">
        { props.children }
      </div>
    </div>
  );
}

Panel.defaultProps = {
  title: undefined,
  icon: undefined,
};

module.exports = {
  Panel,
};
