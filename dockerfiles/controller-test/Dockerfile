FROM ghcr.io/jordimassaguerpla/uyuni/controller-dev:master
COPY testsuite/Gemfile /Gemfile
RUN bundle.ruby2.5 install --gemfile testsuite/Gemfile && \
    npm install cucumber-htmlreporter@5.5.0 --save-dev

