// These functions make use of prototype.js:
// Show form fields and hide table
function showForm(id, name, version, arch, type, editUrl) {
    $('edit-link').update(name);
    $('edit-link').setAttribute('href', editUrl);
    $('image-string').update("\"" + name + "\", Version " + version + " (" + arch + ", " + type + ")");
    $('image-id').setValue(id);
    $('deployment-form').show();
    $('images-table').hide();
}
// Show images table and hide form fields
function showImages() {
    $('images-table').show();
    $('deployment-form').hide();
    $('image-id').setValue('');
    $('image-string').update();
    $('edit-link').setAttribute('href', '');
    $('edit-link').update();
}

