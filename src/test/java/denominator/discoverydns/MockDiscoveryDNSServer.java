package denominator.discoverydns;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.rule.MockWebServerRule;
import denominator.*;
import denominator.assertj.RecordedRequestAssert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static denominator.Credentials.ListCredentials;
import static denominator.assertj.MockWebServerAssertions.assertThat;

final class MockDiscoveryDNSServer extends DiscoveryDNSProvider implements TestRule {

  private final MockWebServerRule delegate = new MockWebServerRule();
  private final X509Certificate certificate;
  private final PrivateKey privateKey;

  MockDiscoveryDNSServer() {
    try {
      this.certificate = DiscoveryDNSTestGraph.getTestX509Certificate();
      this.privateKey = DiscoveryDNSTestGraph.getTestPrivateKey();
      KeyStore serverKeyStore = FeignModule.keyStore(this.certificate, this.privateKey);
      delegate.get().useHttps(FeignModule.sslSocketFactory(serverKeyStore), false);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public String url() {
    return "https://localhost:" + delegate.getPort();
  }

  DNSApiManager connect() {
    return Denominator.create(this, CredentialsConfiguration.credentials(credentials()));
  }

  Credentials credentials() {
    return ListCredentials.from(certificate, privateKey);
  }

  void enqueue(MockResponse mockResponse) {
    delegate.enqueue(mockResponse);
  }

  RecordedRequestAssert assertRequest() throws InterruptedException {
    return assertThat(delegate.takeRequest());
  }

  void shutdown() throws IOException {
    delegate.get().shutdown();
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return delegate.apply(base, description);
  }

  @dagger.Module(injects = DNSApiManager.class, complete = false, includes =
      DiscoveryDNSProvider.Module.class)
  static final class Module {

  }
}
