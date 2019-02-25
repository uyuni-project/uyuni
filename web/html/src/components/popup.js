/* eslint-disable */
"use strict";

const PropTypes = require('prop-types');
const React = require("react");

class PopUp extends React.Component {

  componentDidMount() {
    $("#" + this.props.id).on("hidden.bs.modal", this.props.onClosePopUp);
  }

  close() {
    $("#" + this.props.id).modal().hide();
  }

  render() {

    let bootStrapModalOptionalProps = {};

    if(!this.props.closableModal) {
      bootStrapModalOptionalProps = {
        ...bootStrapModalOptionalProps,
        "data-backdrop": "static",
        "data-keyboard": "false"
      }
    }

    return (
      <div className="modal fade" tabIndex="-1" role="dialog" id={this.props.id} {...bootStrapModalOptionalProps}>
        <div className={"modal-dialog " + (this.props.className ? this.props.className : "")}>
          <div className="modal-content">
            {!this.props.hideHeader
              && (
                <div className="modal-header">
                  {
                    this.props.closableModal &&
                    <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                      <span aria-hidden="true">&times;</span>
                    </button>
                  }
                  {this.props.title ? <h4 className="modal-title">{this.props.title}</h4> : null}
                </div>
              )
            }
            <div className="modal-body">{this.props.content}</div>
            {this.props.footer ? <div className="modal-footer">{this.props.footer}</div> : null}
          </div>
        </div>
      </div>
    );
  }
}

PopUp.propTypes = {
  id: PropTypes.string.isRequired, // the id of the html div tag
  onClosePopUp: PropTypes.func, // a callback function with no parameters
  className: PropTypes.string, // the css className for the 'modal-dialog' div
  title: PropTypes.string,
  content: PropTypes.node, // the body of the popup
  footer: PropTypes.node,
  closableModal: PropTypes.bool,
  hideHeader: PropTypes.bool,
};

PopUp.defaultProps = {
  closableModal: true
}

module.exports = {
  PopUp : PopUp
}
