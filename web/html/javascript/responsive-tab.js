
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

	  // Tomo el tamaño del <ul> contenedor
	  var tabSize = $(".responsive-wizard ul.nav-tabs").width();

	  // creo un array con todos los hijos del UL -> <li> -> seran guardados como objetos
	  var tabListArray = $(".responsive-wizard ul.nav-tabs").children();

	  // cuento la cantidad de tabs que contiene el wizard
	  var tabNumbs = tabListArray.length;

	  // calculo cuantos <li>, del tamaño configurado en settings, entran en el tamaño actual que tiene el <ul>
	  var allowedTabsPerRowDecimals = tabSize / tabSettings.minwidth;

	  // calculo la cantidad de filas que voy a tener
	  var numRowsTabs = Math.ceil(tabNumbs / Math.floor(allowedTabsPerRowDecimals));

	  // calculo cuantos tabs me entran en cada fila
	  // divido la cantidad de tabs que tengo por la cantidad de filas para distribuir 
	  // equitativamente los tabs por filas (para que no quede 1 solo y muy grande en la ultima fila)
	  var tabsPerRow = Math.ceil(tabNumbs / numRowsTabs);

	  // calculo cuantas filas estaran completas
	  var listOfTabs = Math.floor(tabNumbs / tabsPerRow);

	  // calculo la cantidad de tabs que tienen las filas que quedan completas sumadas
	  var numTabsCompleteRow = tabsPerRow * listOfTabs;

	  // calculo el tamaño final de los <li>
	  var liTabsSize = tabSize / tabsPerRow;

	  // inserto en un nuevo array los tabs que quedan en las filas completas
	  var completeRows = $();
	  for (var i = 0; i < numTabsCompleteRow; i++) {
	  	completeRows.push(tabListArray[i]);
	  };

	  // en otro array inserto los tabs que van a quedar desparejos
	  var oddRows = $();
	  for (var i = numTabsCompleteRow; i < tabNumbs; i++) {
	  	oddRows.push(tabListArray[i]);
	  };
	  // si tengo tabs desparejos, les calculo el tamaño
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
