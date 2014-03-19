
// call the resizer on Document Ready and Everytime the screen is resized
function TabResizer (userSettings) {
  // save the object with settings the user sent
  var userTabSetting = userSettings
  // call on resize and document ready
  $( window ).on("resize", tabResponsive);
  $(document).on("ready", tabResponsive());

  function tabResponsive () {
    //setting up properties
    var tabSettingsDefault = {
      minwidth : 160 // the minimum width of a Tab in pixels
    };
    // nesting the default properties with the ones set up by the user
    var tabSettings = $.extend({}, tabSettingsDefault, userTabSetting);

    var tabSize = $(".responsive-wizard ul.nav-tabs").width();
    var tabListArray = $(".responsive-wizard ul.nav-tabs").children();
    var tabNumbs = tabListArray.length;
    var allowedTabsPerRowDecimals = tabSize / tabSettings.minwidth;
    var numRowsTabs = Math.ceil(tabNumbs / Math.floor(allowedTabsPerRowDecimals));
    var tabsPerRow = Math.ceil(tabNumbs / numRowsTabs);
    var listOfTabs = Math.floor(tabNumbs / tabsPerRow);
    var numTabsCompleteRow = tabsPerRow * listOfTabs;
    var liTabsSize = tabSize / tabsPerRow;

    var completeRows = $();
    for (var i = 0; i < numTabsCompleteRow; i++) {
      completeRows.push(tabListArray[i]);
    };

    // odd rows of li
    var oddRows = $();
    for (var i = numTabsCompleteRow; i < tabNumbs; i++) {
      oddRows.push(tabListArray[i]);
    };

    if (oddRows.length > 0) {
      var oddTabsSize = tabSize / oddRows.length;
    }
    
    // apply the new size and corners to the elements in the full rows
    $.each(completeRows, function(elem) {
      switch (elem) {
        case 0: 
        tabCorner = "4px 0 0 0";
        break;
        case (tabsPerRow-1):
        tabCorner = "0 4px 0 0";
        break;
        default : 
        tabCorner = "0 0 0 0";
      }
      applySize($(this), liTabsSize, tabCorner);
    });

    // apply the new size and corners to the elements in the incomplete row
    $.each(oddRows, function() {
      tabCorner = "0 0 0 0";
      applySize($(this), oddTabsSize, tabCorner);
    });
  }

  function applySize (liObj, tabSize, corners) {
    $(liObj).css({
      "width": tabSize + "px",
      "text-align": "center"
    });
    $(liObj).children().css({
      "border-radius": corners
    });
  }
}
