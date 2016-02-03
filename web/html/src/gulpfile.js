/* File: gulpfile.js */

var gulp = require('gulp');
var gutil = require('gulp-util');
var source = require('vinyl-source-stream'); // Used to stream bundle for further handling
var browserify = require('browserify');
var watchify = require('watchify');
//var babelify = require('babelify');
var reactify = require('reactify'); // TODO investigate why babelify hangs
var concat = require('gulp-concat');
var glob  = require('glob');
var es = require('event-stream');
var rename = require('gulp-rename')

//gulp.task('browserify', function() {
//    var bundler = browserify({
//        entries: ['./manager/*.js'], // Only need initial file, browserify finds the deps
//        transform: [reactify], // We want to convert JSX to normal javascript
//        debug: false, // Gives us sourcemapping
//        cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
//    });
////    var watcher  = watchify(bundler);
//
////    return watcher
////        .on('update', function (ids) { // When any files update
////            var updateStart = Date.now();
////            gutil.log('Updating', ids);
////            watcher.bundle() // Create new bundle that uses the cache for high performance
////                .pipe(source('org-state-catalog-app.js'))
////                .pipe(gulp.dest('../javascript/manager'));
////            gutil.log('Updated in', (Date.now() - updateStart) + 'ms');
////        })
//    return bundler
//        .bundle() // Create the initial bundle when starting the task
//        .pipe(source()) // this is a surrogate file name //'org-state-catalog-app.js'
//        .pipe(gulp.dest('../javascript/manager'));
//});

gulp.task('browserify', function(done) {
    glob('./manager/*.js', function(err, files) {
        if(err) done(err);

        var tasks = files.map(function(entry) {
            var bundler = browserify({
                entries: [entry], // Only need initial file, browserify finds the deps
                transform: [reactify], // We want to convert JSX to normal javascript
                debug: false, // Gives us sourcemapping
                cache: {}, packageCache: {}, fullPaths: true // Requirement of watchify
            });

            return bundler
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

// Just running the two tasks
gulp.task('default', ['browserify']);