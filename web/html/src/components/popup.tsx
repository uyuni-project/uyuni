import * as React from "react";

type Props = {
  /** The id of the html div tag */
  id: string;
  /** The css className for the 'modal-dialog' div */
  className?: string;
  title?: React.ReactNode;
  /** The body of the popup */
  content?: React.ReactNode;
  footer?: React.ReactNode;
  hideHeader?: boolean;
  closableModal?: boolean;
  /** A callback function with no parameters */
  onClosePopUp?: () => any;
};

export class PopUp extends React.Component<Props> {
  componentDidMount() {
    if (this.props.onClosePopUp) {
      jQuery("#" + this.props.id).on("hidden.bs.modal", this.props.onClosePopUp);
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
        "data-backdrop": "static",
        "data-keyboard": "false",
      };
    }

    return (
      <div className="modal fade" tabIndex="-1" role="dialog" id={this.props.id} {...bootStrapModalOptionalProps}>
        <div className={"modal-dialog " + (this.props.className ? this.props.className : "")}>
          <div className="modal-content">
            {!this.props.hideHeader && (
              <div className="modal-header">
                {closableModal && (
                  <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
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
