/* File: gulpfile.js */

var gulp = require('gulp');
var gutil = require('gulp-util');
var source = require('vinyl-source-stream');
var browserify = require('browserify');
var babelify = require('babelify');
var glob  = require('glob');
var es = require('event-stream');
var rename = require('gulp-rename')

var bundlerOpts = null;

gulp.task('devel-opts', function(done) {
    bundlerOpts = {
        debug: true, // Gives us sourcemapping
        cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
    };
});

gulp.task('prod-opts', function(done) {
    bundlerOpts = {
        debug: false,
        cache: {}, packageCache: {}, fullPaths: false
    };
});

gulp.task('bundle-manager', function(done) {
    glob('./manager/*.js', function(err, files) {
        if(err) done(err);

        var tasks = files.map(function(entry) {
            bundlerOpts['entries'] = [entry]
            var bundler = browserify(bundlerOpts);

            return bundler
                .transform("babelify", {presets: ["es2015", "react"]})
                .external("react") // exclude react from these bundles
                .bundle() // Create the initial bundle when starting the task
                .pipe(source(entry)) // this is a surrogate file name //'org-state-catalog-app.js'
                .pipe(rename({
                    dirname: '',
                    extname: '.bundle.js'
                }))
                .pipe(gulp.dest('../javascript/manager'));

            });

        es.merge(tasks).on('end', done);
    })
});

gulp.task('default', ['prod-opts', 'bundle-manager']);

gulp.task('devel', ['devel-opts', 'bundle-manager']);
