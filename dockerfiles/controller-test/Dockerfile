FROM ghcr.io/jordimassaguerpla/uyuni/controller-dev:master
COPY testsuite/Gemfile /Gemfile
RUN bundle.ruby2.5 install --gemfile /Gemfile && \
    cucumber init

