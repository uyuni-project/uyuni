jQuery(document).ready(function() {
  jQuery("aside").remove();
  var me = jQuery("section");
  var newMe = jQuery("<div class='login-page'>");
  newMe.html(me.html());
  me.replaceWith(newMe);
  formFocus('loginForm', 'username');
});

