function onDocumentReadyGeneral(){
  // See if there is a system already selected as soon as the page loads
  updateSsmToolbarOpacity();

  // This is a function from spacewalk-checkall.js
  create_checkall_checkbox();

  // Wrapping the tables in a div which will make them responsive
  jQuery(".table").wrap("<div class='table-responsive'>");

  // Set up the behavior and the event function
  // for the spacewalk section toolbar [sst]
  handleSst();

  // Show character length for textarea
  addTextareaLengthNotification();

  scrollTopBehavior();
}

function adaptFluidColLayout() {
  jQuery('.col-class-calc-width').each(function() {
    var totalWidth = jQuery(this).parent().width();
    jQuery(this).siblings('.col').each(function() {
      totalWidth = Math.floor(totalWidth - jQuery(this).outerWidth());
    });
    jQuery(this).css('width', totalWidth - 10);
  });
}

/* Getting the screen size to create a fixed padding-bottom in the Section tag to make both columns the same size */
// On window load and resize
jQuery(window).on("load resize", alignContentDimensions);

// On section#spacewalk-content scroll
function scrollTopBehavior() {
  jQuery(window).on("scroll", function () {
    if(jQuery(this).scrollTop() > 100) {
      jQuery('#scroll-top').show();
    } else {
      jQuery('#scroll-top').hide();
    }

    sstScrollBehavior();
  });

  jQuery(document).on('click', '#scroll-top', function() {
    window.scrollTo(0,0);
  });
}

// A container function for what should be fired
// to set HTML tag dimensions
function alignContentDimensions() {
  adaptFluidColLayout();
  adjustDistanceForFixedHeader();
  sstStyle();
  columnHeight();
}

// empty function by default hooked on window.scroll event
var sstScrollBehavior = function() {
  return;
}

var sstScrollBehaviorSetupIsDone = false; // flag to implement the function one time only
function sstScrollBehaviorSetup(sst) {
  sstScrollBehaviorSetupIsDone = true;
  const adjustSpaceObject = jQuery('<div>').height(sst.outerHeight());
  var fixedTop = sst.offset().top;

  // override the empty function hooked on window.scroll event                                              │
  sstScrollBehavior = function() {
    var currentScroll = jQuery(window).scrollTop();
    if (currentScroll >= fixedTop) {
      sst.after(adjustSpaceObject);
      jQuery(sst).addClass('fixed');
    } else {
      jQuery(sst).removeClass('fixed');
      adjustSpaceObject.remove();
    }
    sstStyle();
  }
}

// when the page scrolls down and the toolbar is going up and hidden,
// the toolbar takes a fixed place right below the header bar
function handleSst() {
  var sst = jQuery('.spacewalk-section-toolbar');

  if (jQuery('.move-to-fixed-toolbar').length > 0) {
    // if there is no 'spacewalk-section-toolbar', then create it
    if (sst.length == 0) {
      sst = jQuery('<div class="spacewalk-section-toolbar">');
      jQuery('.spacewalk-list.list').before(sst);
    }

    // move each named tag into the 'spacewalk-section-toolbar'
    jQuery('.move-to-fixed-toolbar').each(function() {
      sst.append(jQuery(this));
      jQuery(this).removeClass('move-to-fixed-toolbar');
    });
  }

  // move children of each named tag
  // into the 'spacewalk-section-toolbar > action-button-wrapper'
  if (jQuery('.move-children-to-fixed-toolbar').length > 0) {
    // if there is no 'spacewalk-section-toolbar', then create it
    if (sst.length == 0) {
      sst = jQuery('<div class="spacewalk-section-toolbar">');
      jQuery('.spacewalk-list.list').before(sst);
    }
    var selectorButtonWrapper = jQuery('.selector-button-wrapper');
    // if there is no 'action-button-wrapper', then create it
    if (selectorButtonWrapper.length == 0) {
      selectorButtonWrapper = jQuery('<div class="selector-button-wrapper">');
      sst.prepend(selectorButtonWrapper);
    }
    jQuery('.move-children-to-fixed-toolbar').each(function() {
      selectorButtonWrapper.append(jQuery(this).children());
      jQuery(this).removeClass('move-children-to-fixed-toolbar');
    });
  }

  // setup the function only if there is the 'spacewalk-section-toolbar'
  // and the function is not yet setup
  if (!sstScrollBehaviorSetupIsDone && sst.length > 0) {
    sstScrollBehaviorSetup(sst);
  }
}

function sstStyle() {
  var sst = jQuery('.spacewalk-section-toolbar');
  if (sst.hasClass('fixed')) {
    sst.css({
      top: jQuery('header').outerHeight() - 1,
      left: jQuery('section').offset().left,
      'min-width': jQuery('section').outerWidth()
    });
  }
  else {
    sst.css({
      'min-width': 0
    });
  }
  sst.width(sst.parent().width() - sst.css('padding-left') - sst.css('padding-right'));
}

// Header is fixed, the main content column needs
// padding-top equals the header height to be fully visible
function adjustDistanceForFixedHeader() {
  // subtract 1px in case the outerHeight comes to us already upper rounded
  jQuery('body').css('padding-top', jQuery('header').outerHeight());
}

// Make columns 100% in height
function columnHeight() {
  const aside = jQuery('.spacewalk-main-column-layout aside');
  const navToolBox = jQuery('.spacewalk-main-column-layout aside .nav-tool-box');
  const headerHeight = jQuery('header').outerHeight();
  const footerHeight = jQuery('footer').outerHeight();
  const winHeight = jQuery(window).height();
  // // Column height should equal the window height minus the header and footer height
  aside.css('height', winHeight - headerHeight);
  // aside.css('padding-bottom', footerHeight);
  const nav = jQuery('.spacewalk-main-column-layout aside #nav nav ul.level1');
  nav.css('height', aside.outerHeight() - navToolBox.outerHeight() - footerHeight);
};

jQuery(document).on('click', '.navbar-toggle', function() {
  jQuery('aside').toggleClass('in');
  jQuery('aside').toggleClass('collapse');
  columnHeight();
});

// returns an object that can be passed to DWR renderer as a callback
// puts rendered HTML in #divId, opens an alert with the same text if
// debug is true
function makeRendererHandler(divId, debug) {
  return makeAjaxHandler(function(text) {
    if (debug) {
      alert(text);
    }
    jQuery('#' + divId).html(text);
    jQuery('#' + divId).fadeIn();
    columnHeight();
  });
}

// returns an object that can be passed to DWR as a callback
// callbackFunction: function to call when AJAX requests succeeds
// errorHandlerFunction: function to call when AJAX requests fail
// (can be omitted for showFatalError)
// works around a DWR bug calling errorHandler when navigating away
// from a page during an AJAX request
function makeAjaxHandler(callbackFunction, errorHandlerFunction) {
    errorHandlerFunction = typeof errorHandlerFunction !== "undefined" ?
      errorHandlerFunction : showFatalError;

    // workaround to a DWR bug that calls errorHandler when user
    // navigates away from page during an AJAX call
    // first, we detect page unloading
    jQuery(window).on("beforeunload", function() {
      jQuery.unloading = true;
    });
    return {
      callback: callbackFunction,
      errorHandler: function(message, exception) {
        // second, if we get an error during unloading we ignore it
        if (jQuery.unloading == true) {
          console.log("Ignoring exception " + exception + " with message " + message + " because it is a DWR error during unload");
        }
        else {
          errorHandlerFunction(message, exception);
        }
      }
    }
}

// shows a fatal DWR/AJAX error
function showFatalError(message, exception) {
  console.log("DWR AJAX call failed with message: " + message);
  console.log(exception);
  alert("Unexpected error, please reload the page and check server logs.");
}


// Extension to Twitter Bootstrap.
// Gives you a col-XX-auto class like Bootstrap
// That dynamically adjust the grid for the columns to take
// as much space as possible while still being responsive
// So three col-md-auto would get col-md-4 each.
// Five col-md-auto would get two with col-md-3 and three with col-md-2
function onDocumentReadyAutoBootstrapGrid() {
  jQuery.each(['xs', 'sm', 'md', 'lg'], function(idx, gridSize) {
    //for each div with class row
    jQuery('.col-' + gridSize + '-auto:first').parent().each(function() {
      //we count the number of childrens with class col-md-6
      var numberOfCols = jQuery(this).children('.col-'  + gridSize + '-auto').length;
      if (numberOfCols > 0 && numberOfCols < 13) {
        minSpan = Math.floor(12 / numberOfCols);
        remainder = (12 % numberOfCols);
        jQuery(this).children('.col-' + gridSize + '-auto').each(function(idx, col) {
          var width = minSpan;
          if (remainder > 0) {
            width += 1;
            remainder--;
          }
          jQuery(this).addClass('col-' + gridSize + '-' + width);
        });
      }
    });
  });
}

// Humanizes all the time elements with the human class
function humanizeDates() {
  // should be consistent with UserPreferencesUtils.java
  moment.lang(window.preferredLocale, {
    longDateFormat : {
      LT: "HH:mm",
      LTS: "HH:mm:ss",
      L: "YYYY-MM-DD",
      LL: "YYYY-MM-DD",
      LLL: "YYYY-MM-DD",
      LLLL: "YYYY-MM-DD"
    },
  });

  jQuery("time.human-from, time.human-calendar").each(function (index) {
    var datetime = jQuery(this).attr('datetime');
    if (datetime == undefined) {
      // if the attribute is not set, the content
      // should be a valid date
      datetime = jQuery(this).html();
    }
    var parsed = moment(datetime);
    if (parsed.isValid()) {
      var originalContent = jQuery(this).html();
      if (jQuery(this).hasClass("human-from")) {
        var ref = jQuery(this).attr("data-reference-date");
        if (ref) {
          var refParsed = moment(ref);
          if (refParsed.isValid()) {
            jQuery(this).html(parsed.from(refParsed));
          }
        }
        else {
          jQuery(this).html(parsed.fromNow());
        }
      }
      if (jQuery(this).hasClass("human-calendar")) {
        jQuery(this).html(parsed.calendar());
      }
      // if the original did not had a datetime attribute, add it
      var datetimeAttr = jQuery(this).attr('datetime');
      if (datetimeAttr == undefined) {
        jQuery(this).attr('datetime', datetime);
      }
      // add a tooltip
      jQuery(this).attr('title', originalContent);
    }
  });
}

/**
 * Setups ACE editor in a textarea element
 * textarea is a jQuery object
 * mode is the language mode, if emmpty
 * shows a select box to choose it.
 */
function setupTextareaEditor(textarea, mode) {
  // if textarea is not shown, the height will be negative,
  // so we set the height of the editor in the popup to the 70% of the window height
  var tH = textarea.height() > 0 ? textarea.height() : (jQuery(window).height()  * 0.7);

  var editDiv = jQuery('<div>', {
      position: 'absolute',
      width: textarea.width(),
      height: tH,
      'class': textarea.attr('class')
  }).attr('id', textarea.attr('id') + '-editor').insertBefore(textarea);

  var toolBar = jQuery('<div></div>').insertBefore(editDiv[0]);
  textarea.hide();

  var editor = ace.edit(editDiv[0]);
  editor.getSession().setValue(textarea.val());
  editor.getSession().setOptions({ tabSize: 4, useSoftTabs: true });

  editor.setTheme("ace/theme/xcode");
  editor.getSession().setMode("ace/mode/sh");

  // before submitting the code, the textarea
  // should be updated with the editor value
  textarea.closest('form').submit(function () {
      textarea.val(editor.getSession().getValue());
  })

  toolBar.addClass('ace_editor');
  toolBar.css('width', editDiv.css('width'));
  var modeSel = jQuery('<select> \
    <option selected value="sh">Shell</option> \
    <option value="xml">XML</option> \
    <option value="ruby">Ruby</option> \
    <option value="python">Python</option> \
    <option value="perl">perl</option> \
    <option value="yaml">Yaml</option> \
    </select>');
  modeSel.find('option').each(function() {
  if (jQuery(this).text() == mode)
    jQuery(this).attr('selected', 'selected');
  });

  toolBar.append(modeSel);
  if (mode != "") {
    editor.getSession().setMode("ace/mode/" + mode);
    toolBar.hide();
  }

  modeSel.change(function () {
    editor.getSession().setMode("ace/mode/" + jQuery(this).val());
  });

  // Set editor to read only according to data attribute
  editor.setReadOnly(textarea.data('readonly') || textarea.attr('readonly'));

  editor.getSession().on('change', function() {
    textarea.val(editor.getSession().getValue());
  });
}

/**
 * setups every textarea with data-editor attribute
 * set to some language with an ACE editor
 */
jQuery(function () {
  jQuery('textarea[data-editor]').each(function () {
    var textarea = jQuery(this);
    var mode = textarea.data('editor');
    setupTextareaEditor(textarea, mode);
  });
});

// Disables the enter key from submitting the form
function disableEnterKey() {
  jQuery(window).on('keydown', function(event){
    if(event.keyCode == 13) {
      event.preventDefault();
      return false;
    }
  });
}

// Binds the enter key to a specific submit button on a key event
function enterKeyHandler(event, $button) {
    if(event.keyCode == 13) {
        $button.trigger("click");
        return false;
    }
}

function adjustSpacewalkContent() {
  alignContentDimensions();
  // trigger the 'spacewalk-section-toolbar' (sst) handler on content change
  // since it can happen that the sst has just been added right now
  handleSst();
}

/*
* Create an Observer object that monitors if something in the HTML has changes,
* if that happens it fires the window resize computation event
* https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver
*/
// create an observer instance
var spacewalkContentObserver = new MutationObserver(function(mutations) {
    if (mutations.length > 0) {
      adjustSpacewalkContent();
    }
});

function registerSpacewalkContentObservers() {
  var target = document.getElementById('spacewalk-content');
  // configuration of the observer:
  var config = { childList: true, characterData: true, subtree: true };
  // pass in the target node, as well as the observer options
  spacewalkContentObserver.observe(target, config);
}

jQuery(document).on('click', '.toggle-box', function() {
  if (jQuery(this).hasClass('open')) {
    jQuery(this).removeClass('open');
  }
  else {
    jQuery('.toggle-box.open').trigger('click');
    jQuery(this).addClass('open');
  }
  jQuery(this).blur(); // remove the focus
})

// focus go away from the menu or the nav menu
jQuery(document).on("click", function (e) {
  var target = jQuery(e.target);
  // if a toggle-box button is active and the current click
  // is not on its related box, trigger a close for it
  jQuery('.toggle-box.open').each(function() {
    var toggleButton = jQuery(this);
    var toggleBox = toggleButton.parent();
    if (!target.closest(toggleBox).length) {
      toggleButton.trigger('click');
    }
  });
});

/* prevent jumping to the top of the page because
of an <a href> tag that is actually not a link */
jQuery(document).on('click', 'a', function(e) {
  const href = jQuery(this).attr('href');
  if (href != null && href.length == 1 && href == '#') {
    e.preventDefault();
    jQuery(this).blur(); // remove the focus
  }
});

/*
* Check if the field contains the allowed values only
*/
jQuery(document).on('keyup change', '.activationKey-check', function(e) {
  if (jQuery(this).val().match(/([^a-zA-Z0-9-_.])/g)) {
    jQuery(this).parent().addClass('has-error');
  }
  else {
    jQuery(this).parent().removeClass('has-error');
  }
});

function addTextareaLengthNotification() {
  // Add a notification text of the remaining length for a textarea
  jQuery('textarea.with-maxlength').each(function() {
    const textareaId = jQuery(this).attr('id');
    jQuery(this).after(
      jQuery('<div/>')
        .attr("id", "newDiv1")
        .addClass("remaining-length-wrapper text-right")
        .html(
          jQuery('<span/>')
            .html([
              jQuery('<span/>')
              .attr("id", textareaId + '-remaining-length')
              .text(jQuery(this).attr('maxlength') - jQuery(this).val().length)
              , jQuery('<span/>').text(' ' + t('remaining'))
            ])
        )
    );
  });

  // Update the remaining length text of the related textarea
  jQuery(document).on('input', 'textarea.with-maxlength', function() {
    jQuery('#' + jQuery(this).attr('id') + '-remaining-length')
      .html(jQuery(this).attr('maxlength') - jQuery(this).val().length);
  });
}

function initIEWarningUse() {
  if(window.document.documentMode) {
    const bodyContentNode = document.getElementById("spacewalk-content");
    if(bodyContentNode) {
      let ieWarningNode = document.createElement("div");
      ieWarningNode.className = 'alert alert-warning';
      ieWarningNode.innerHTML = t(
          "The browser Internet Explorer is not supported. " +
          "Try using Firefox, Chrome or Edge"
      );

      bodyContentNode.insertBefore(
          ieWarningNode,
          bodyContentNode.firstChild
      )
    }
  }
}

// Function used to initialize old JS behaviour. (After react load/spa transition)
// Please, don't add anything else to this file. The idea is to get rid of this file while migrating everything to react.
function onDocumentReadyInitOldJS() {
  sstScrollBehaviorSetupIsDone = false;
  onDocumentReadyGeneral();
  onDocumentReadyAutoBootstrapGrid();
  humanizeDates();
  initIEWarningUse();
}

jQuery(document).ready(function() {
  onDocumentReadyGeneral();
  onDocumentReadyAutoBootstrapGrid();
  registerSpacewalkContentObservers();
  humanizeDates();
  initIEWarningUse();
});
