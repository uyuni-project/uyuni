
$(function() {
  $('#scc-start-migration-btn').click(function() {
    var ret = SCCConfigAjax.sayHello(makeAjaxHandler(function(ret) {
        alert(ret);
    }));
  });
});
