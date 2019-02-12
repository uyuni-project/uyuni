// @flow

import { hot } from 'react-hot-loader';
import React from 'react';
import { Loading } from 'components/loading/loading';
import { PopUp } from 'components/popup';
import { Button } from 'components/buttons';
import { Form } from 'components/input/Form';
import { Password } from 'components/input/Password';
import { showDialog } from 'components/dialog/util';
import { hideDialog } from 'components/dialog/util';
import { VncClient } from './vnc-client';
import type { ConsoleClientType } from './guests-console-types';
import styles from './guests-console.css';

type Props = {
  hostId: string,
  guestUuid: string,
  guestName: string,
  graphicsType: string,
  socketUrl: string,
};

type State = {
  error: ?string,
  expanded: boolean,
  connected: boolean,
  popupState: string,
  password: ?string,
};

class GuestsConsole extends React.Component<Props, State> {
  client: ConsoleClientType;
  inhibitPopup: boolean;
  popupSubmit: Function;

  constructor(props: Props) {
    super(props);

    const clients = {
      vnc: VncClient,
    };
    this.client = new clients[this.props.graphicsType]('canvas', this.props.socketUrl, this.onConnect, this.onDisconnect, this.askPassword);

    const editUrl = `/rhn/manager/systems/details/virtualization/guests/${this.props.hostId}/edit/${this.props.guestUuid}`;
    const error = this.client !== undefined
      ? undefined
      : t(`Can not show display. Ensure, the virtual machine is stopped, <a href='${editUrl}'>set the display</a> to VNC and start again`);

    this.state = {
      error,
      expanded: false,
      connected: false,
      popupState: 'wait',
      password: undefined,
    };
  }

  componentDidMount() {
    this.connect();

    window.onbeforeunload = () => {
      this.inhibitPopup = true;
    };
  }

  connect = () => {
    if (this.client != null) {
      this.client.connect();
    }
    this.popupSubmit = undefined;

    this.setState({
      popupState: 'wait',
      password: undefined,
    }, this.showPopup);
  }

  onConnect = () => {
    hideDialog('popup');
    this.setState({
      expanded: false,
      connected: true,
    });
  }

  onDisconnect = (error: ?string) => {
    const message = error != null ? error : t('Disconnected');
    const connectionError = t('Failed to connect');
    this.setState((oldState) => {
      const errorMessage = (oldState.connected ? message : connectionError);
      return {
        error: errorMessage,
        connected: false,
      };
    });
    if (!this.inhibitPopup) {
      this.popupSubmit = this.connect;
      this.setState({ popupState: 'errors' }, this.showPopup);
    } else {
      hideDialog('popup');
    }
  }

  toggleScale = () => {
    this.setState((state) => {
      const expanded = !state.expanded;
      if (this.client != null) {
        this.client.toggleScale(expanded);
      }
      return Object.assign({}, state, { expanded });
    });
  }

  showPopup = () => {
    showDialog('popup');
  }

  askPassword = () => new Promise((resolve) => {
    this.popupSubmit = () => {
      hideDialog('popup');
      resolve(this.state.password);
    };
    this.setState({ popupState: 'askPassword' }, this.showPopup);
  });

  render() {
    const buttonValues = {
      askPassword: {
        text: t('Submit'),
      },
      errors: {
        text: t('Retry'),
      },
    }[this.state.popupState];
    const canResize = this.client != null && this.client.canResize;
    const popupContent = () => {
      if (this.state.popupState === 'wait') {
        return <Loading text={t('Connecting...')} withBorders={false} />;
      }
      if (this.state.popupState === 'askPassword') {
        return (
          <Form model={this.state} className="form-horizontal">
            <Password
              name="password"
              label={t('Password')}
              labelClass="col-md-3"
              divClass="col-md-6"
            />
          </Form>
        );
      }
      return this.state.error;
    };
    const areaClassName = `display_area_${this.props.graphicsType}`;
    return (
      <>
        <header className={`navbar-pf ${styles.navbar_pf_console}`}>
          <div className={`navbar-header ${styles.navbar_header_console}`}>
            <i className="fa spacewalk-icon-virtual-guest" />
            {this.props.guestName}
          </div>
          <ul className="nav navbar-nav navbar-utility">
            <li>
              <Button
                title="Toggle full size"
                icon={this.state.expanded ? 'fa-compress' : 'fa-expand'}
                handler={this.toggleScale}
                disabled={!this.state.connected || !canResize}
              />
            </li>
          </ul>
        </header>
        <PopUp
          id="popup"
          hideHeader
          content={popupContent()}
          footer={
            buttonValues !== undefined && [
              <Button
                key="submit"
                className="btn-primary"
                text={buttonValues.text}
                title={buttonValues.text}
                handler={() => {
                  if (this.popupSubmit != null) {
                    this.popupSubmit();
                  }
                }}
              />,
              <Button
                key="cancel"
                className="btn-default"
                text={t('Cancel')}
                title={t('Cancel')}
                handler={() => hideDialog('popup')}
              />,
            ]
          }
        />
        <div
          id="display-area"
          className={`${styles.display_area_console} ${styles[areaClassName]}`}
        >
          <div id="canvas" className={styles.canvas}/>
        </div>
      </>
    );
  }
}

export default hot(module)(GuestsConsole);
