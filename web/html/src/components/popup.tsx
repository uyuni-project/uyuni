/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { type ReactNode, Component } from "react";
type Props = {
  /** The id of the html div tag */
  id: string;
  /** The css className for the 'modal-dialog' div */
  className?: string;
  title?: ReactNode;
  /** The body of the popup */
  content?: ReactNode;
  footer?: ReactNode;
  hideHeader?: boolean;
  closableModal?: boolean;
  /** A callback function with no parameters */
  onClosePopUp?: () => any;
};

export class PopUp extends Component<Props> {
  private handleClose = () => this.props.onClosePopUp?.();

  componentDidMount() {
    jQuery("#" + this.props.id).on("hidden.bs.modal", this.handleClose);
  }

  componentWillUnmount() {
    const element = document.getElementById(this.props.id);
    if (!element) return;
    jQuery(element).off("hidden.bs.modal", this.handleClose);
    if (typeof bootstrap !== "undefined") {
      const modal = bootstrap.Modal.getInstance(element);
      modal?.hide();
      modal?.dispose();
    }
  }

  close() {
    jQuery("#" + this.props.id)
      .modal()
      .hide();
  }

  render() {
    let bootStrapModalOptionalProps: any = {};

    const closableModal = this.props.closableModal ?? true;

    if (!closableModal) {
      bootStrapModalOptionalProps = {
        ...bootStrapModalOptionalProps,
        "data-bs-backdrop": "static",
        "data-bs-keyboard": "false",
      };
    }

    return (
      <div className="modal" tabIndex="-1" role="dialog" id={this.props.id} {...bootStrapModalOptionalProps}>
        <div className={"modal-dialog " + (this.props.className ? this.props.className : "")}>
          <div className="modal-content">
            {!this.props.hideHeader && (
              <div className="modal-header">
                {closableModal && (
                  <button type="button" className="close" data-bs-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">
                      <i className="fa fa-close"></i>
                    </span>
                  </button>
                )}
                {this.props.title ? <h4 className="modal-title">{this.props.title}</h4> : null}
              </div>
            )}
            <div className="modal-body">{this.props.content}</div>
            {this.props.footer ? <div className="modal-footer">{this.props.footer}</div> : null}
          </div>
        </div>
      </div>
    );
  }
}
