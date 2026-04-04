import { type ReactNode, Component } from "react";

import { DangerDialog } from "./DangerDialog";

type Props = {
  id: string;
  type: string;
  name: string;
  itemName: string;
  icon: string;
  selected: any[];
  onConfirm: (type: string, selected: any[], forceState: any) => any;
  canForce: boolean;
  forceName?: string;
  onClose: () => void;
  children?: ReactNode;
  /** whether the dialog should be shown or hidden */
  isOpen: boolean;
};

type State = {
  force: boolean;
};

/**
 * A pop-up dialog to confirm an action on a selection of items.
 * This is based on the DangerDialog, but adds text to it and a force
 * checkbox.
 * Related items are passed to the 'selected' property as an array. Each
 * item is expected to have a 'name' property.
 */
export class ActionConfirm extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      force: false,
    };
  }

  closePopUp = () => {
    this.setState({ force: false });
    this.props.onClose();
  };

  render() {
    return (
      <DangerDialog
        isOpen={this.props.isOpen}
        id={this.props.id}
        title={t(
          `${this.props.name} ${
            this.props.selected.length === 1 ? this.props.itemName : `Selected ${this.props.itemName}(s)`
          }`
        )}
        content={
          <>
            {this.props.children}
            {this.props.canForce && (
              <p>
                <input
                  type="checkbox"
                  id="force"
                  checked={this.state.force}
                  onChange={(event) => this.setState({ force: event.target.checked })}
                />
                <label htmlFor="force">{this.props.forceName}</label>
              </p>
            )}
            {this.props.selected.length === 1 && (
              <span>
                {/* TODO: Here and below, this translation logic needs to be changed to whole sentences from parents */}
                {t("Are you sure you want to {action} {name}", {
                  action:
                    this.state.force && this.props.forceName
                      ? this.props.forceName.toLowerCase()
                      : this.props.name.toLowerCase(),
                  name: this.props.itemName.toLowerCase(),
                })}
                <strong>{this.props.selected[0].name}</strong>?
              </span>
            )}
            {this.props.selected.length > 1 && (
              <span>
                {t("Are you sure you want to {action} the selected {name}s? ({count} {name}s selected)", {
                  action:
                    this.state.force && this.props.forceName
                      ? this.props.forceName.toLowerCase()
                      : this.props.name.toLowerCase(),
                  name: this.props.itemName.toLowerCase(),
                  count: this.props.selected.length,
                })}
                ?
              </span>
            )}
          </>
        }
        onConfirm={() =>
          this.props.onConfirm(
            this.props.type,
            this.props.selected,
            this.props.canForce ? { force: this.state.force } : {}
          )
        }
        onClose={() => this.closePopUp()}
        submitText={this.state.force && this.props.forceName ? this.props.forceName : this.props.name}
        submitIcon={this.props.icon}
      />
    );
  }
}
