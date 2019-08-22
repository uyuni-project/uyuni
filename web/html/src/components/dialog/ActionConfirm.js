// @flow

import React from "react";
import { DangerDialog } from "./DangerDialog";

import type {Node} from 'react';

type Props =  {
  id: string,
  type: string,
  name: string,
  itemName: string,
  icon: string,
  selected: Object[],
  fn: Function,
  canForce: boolean,
  forceName?: string,
  onClose?: Function,
  children?: Node,
};

type State = {
  force: boolean,
};

/**
 * A pop-up dialog to confirm an action on a selection of items.
 * This is based on the DangerDialog, but adds text to it and a force
 * checkbox.
 * Related items are passed to the ''selected' property as an array. Each
 * item is expected to have a 'name' property.
 */
export class ActionConfirm extends React.Component<Props, State> {

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
        title={t(`${this.props.name} ${this.props.selected.length === 1 ? this.props.itemName : `Selected ${this.props.itemName}(s)`}`)}
        content={(
          <>
            { this.props.children }
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
                {t('Are you sure you want to {0} {1} ',
                 this.state.force && this.props.forceName
                  ? this.props.forceName.toLowerCase() : this.props.name.toLowerCase(),
                 this.props.itemName.toLowerCase())}
                <strong>{this.props.selected[0].name}</strong>
                ?
              </span>
            }
            { this.props.selected.length > 1 &&
              <span>
                {t('Are you sure you want to {0} the selected {1}s? ({2} {1}s selected)',
                  this.state.force && this.props.forceName
                    ? this.props.forceName.toLowerCase() : this.props.name.toLowerCase(),
                  this.props.itemName.toLowerCase(),
                  this.props.selected.length)}
                ?
              </span>
            }
          </>
        )}
        onConfirm={() => this.props.fn(this.props.type, this.props.selected,
                                       this.props.canForce ? {force: this.state.force} : {})}
        onClosePopUp={() => this.closePopUp()}
        submitText={this.state.force && this.props.forceName ? this.props.forceName : this.props.name}
        submitIcon={this.props.icon}
      />
    );
  }
}
