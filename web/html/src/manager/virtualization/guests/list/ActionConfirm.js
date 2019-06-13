// @flow

const React = require('react');
const { DangerDialog } = require('components/dialog/DangerDialog');

type Props =  {
  id: string,
  type: string,
  name: string,
  icon: string,
  selected: Object[],
  fn: Function,
  canForce: boolean,
  forceName?: string,
  onClose?: Function,
};

type State = {
  force: boolean,
};

class ActionConfirm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      force: false,
    };
  }

  closePopUp = () => {
    this.setState({force: false});
    if (this.props.onClose) {
      this.props.onClose();
    }
  }

  render() {
    return (
      <DangerDialog
        id={this.props.id}
        title={t(`${this.props.name} ${this.props.selected.length === 1 ? 'Guest' : 'Selected Guest(s)'}`)}
        content={(
          <>
            { this.props.canForce &&
              <p>
                <input
                  type="checkbox"
                  id="force"
                  defaultChecked={false}
                  checked={this.state.force}
                  onChange={event => this.setState({force: event.target.checked})}
                />
                <label htmlFor="force">{this.props.forceName}</label>
              </p>
            }
            { this.props.selected.length === 1 &&
              <span>
                {t('Are you sure you want to {0} guest ',
                 this.state.force && this.props.forceName
                  ? this.props.forceName.toLowerCase() : this.props.name.toLowerCase())}
                <strong>{this.props.selected[0].name}</strong>
                ?
              </span>
            }
            { this.props.selected.length > 1 &&
              <span>
                {t('Are you sure you want to {0} the selected guests? ({1} guests selected)',
                  this.state.force && this.props.forceName
                    ? this.props.forceName.toLowerCase() : this.props.name.toLowerCase(),
                  this.props.selected.length)}
                ?
              </span>
            }
          </>
        )}
        onConfirm={() => this.props.fn(this.props.type, this.props.selected.map(item => item.uuid),
                                       this.props.canForce ? {force: this.state.force} : {})}
        onClosePopUp={() => this.closePopUp()}
        submitText={this.state.force && this.props.forceName ? this.props.forceName : this.props.name}
        submitIcon={this.props.icon}
      />
    );
  }
}

module.exports = {
  ActionConfirm,
}
