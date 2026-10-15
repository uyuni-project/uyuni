Test-only TLS material for `MqttTlsSocketFactoryTest` and `MqttPublisherServiceTest`.

- `ca.pem`: self-signed CA certificate, valid until 2126
- `client.p12`: client certificate signed by that CA, with its key; password `test-password`
- `not-a-certificate.pem`: a file with no certificate in it, for the error path

The private keys protect nothing and must never be used outside these tests.
