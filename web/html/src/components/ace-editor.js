'use strict';

var React = require("react");

class AceEditor extends React.Component {

  propTypes : {
    mode: React.PropTypes.string,
    content: React.PropTypes.string,
    className: React.PropTypes.string,
    id: React.PropTypes.string,
    minLines: React.PropTypes.number,
    maxLines: React.PropTypes.number,
    readOnly: React.PropTypes.bool,
    name: React.PropTypes.string,
    onChange: React.PropTypes.func,
  }

  componentDidMount() {
    const component = this;

    const node = React.findDOMNode(component.refs.editor);
    const editor = ace.edit(node);
    editor.setTheme("ace/theme/xcode");
    editor.getSession().setMode("ace/mode/" + component.props.mode);
    editor.setShowPrintMargin(false);
    editor.setOptions({minLines: component.props.minLines});
    editor.setOptions({maxLines: component.props.maxLines});
    editor.setReadOnly(component.props.readOnly);

    editor.getSession().on('change', function() {
      component.props.onChange(editor.getSession().getValue());
    });
  }

  render() {
    return (
      <div ref="editor" className={this.props.className} id={this.props.id}>
        {this.props.content}
      </div>
    );
  }
}

module.exports = {
    AceEditor : AceEditor
}
