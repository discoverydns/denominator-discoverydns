package denominator;

import denominator.discoverydns.DiscoveryDNSProvider;
import denominator.model.ResourceRecordSet;
import denominator.model.rdata.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.X509CertParser;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static denominator.CredentialsConfiguration.credentials;
import static denominator.model.ResourceRecordSets.*;
import static feign.Util.emptyToNull;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;

public class DiscoveryDNSTestGraph extends TestGraph {

  private static final String url = emptyToNull(getProperty("discoverydns.url"));
  private static final String zone = emptyToNull(getProperty("discoverydns.zone"));

  /**
   * Lazy initializing manager
   */
  public DiscoveryDNSTestGraph() {
    super(null, zone);
  }

  /**
   * Since discoverydns uses client auth, it cannot be eagerly initialized. This lazy initializes
   * it, if the required credentials are present.
   */
  @Override
  protected DNSApiManager manager() {
    Object credentials = credentials(getTestX509Certificate(), getTestPrivateKey());
    DiscoveryDNSProvider discoveryDNSProvider = new DiscoveryDNSProvider(url);
    discoveryDNSProvider.getLocalConfiguration().useDefaultConfiguration();
    return Denominator.create(discoveryDNSProvider, credentials, new DNSApiManagerFactory.HttpLog());
  }

  @Override
  List<ResourceRecordSet<?>> basicRecordSets(Class<?> testClass) {
    return filterRecordSets(testClass, manager().provider().basicRecordTypes());
  }

  private List<ResourceRecordSet<?>> filterRecordSets(Class<?> testClass,
                                                      Collection<String> types) {
    List<ResourceRecordSet<?>> filtered = new ArrayList<ResourceRecordSet<?>>();
    for (Map.Entry<String, ResourceRecordSet<?>> entry : stockRRSets(testClass).entrySet()) {
      if (types.contains(entry.getKey())) {
        filtered.add(entry.getValue());
      }
    }
    return filtered;
  }

  /**
   * Creates sample record sets named base on the {@code testClass}.
   */
  private Map<String, ResourceRecordSet<?>> stockRRSets(Class<?> testClass) {
    String rPrefix = testClass.getSimpleName().toLowerCase() + "."
        + getProperty("user.name").replace('.', '-');
    String rSuffix = rPrefix + "." + DiscoveryDNSTestGraph.zone;
    if (!rSuffix.endsWith(".")) {
      rSuffix = rSuffix + ".";
    }
    String dSuffix = rSuffix;

    Map<String, ResourceRecordSet<?>> result = new LinkedHashMap<String, ResourceRecordSet<?>>();
    result.put("A", a("ipv4-" + rSuffix, asList("192.0.2.1", "198.51.100.1", "203.0.113.1")));
    result.put("AAAA",
        aaaa("ipv6-" + rSuffix, asList("2001:1DB8:85A3:1001:1001:8A2E:1371:7334",
            "2001:1DB8:85A3:1001:1001:8A2E:1371:7335",
            "2001:1DB8:85A3:1001:1001:8A2E:1371:7336")));
    result.put("CNAME",
        cname("www-" + rSuffix,
            asList("www-north-" + dSuffix, "www-east-" + dSuffix, "www-west-"
                + dSuffix)));
    result.put("CERT",
        ResourceRecordSet.<CERTData>builder().name("cert-" + rSuffix).type("CERT")
            .add(CERTData.builder().format(1).tag(2).algorithm(3).certificate("ABCD")
                .build())
            .add(CERTData.builder().format(1).tag(2).algorithm(3).certificate("EFGH")
                .build())
            .build());
    result.put("MX",
        ResourceRecordSet.<MXData>builder().name("mail-" + rSuffix).type("MX")
            .add(MXData.create(10, "mail1-" + dSuffix))
            .add(MXData.create(10, "mail2-" + dSuffix))
            .add(MXData.create(10, "mail3-" + dSuffix)).build());
    result.put("NS",
        ns("ns-" + rSuffix,
            asList("ns1-" + dSuffix, "ns2-" + dSuffix, "ns3-" + dSuffix)));
    result.put("NAPTR",
        ResourceRecordSet.<NAPTRData>builder().name("naptr-" + rSuffix).type("NAPTR")
            .add(NAPTRData.builder().order(1).preference(1).flags("U").services("E2U+sip")
                .regexp("!^.*$!sip:customer-service@example.com!").replacement(".")
                .build())
            .add(NAPTRData.builder().order(2).preference(1).flags("U").services("E2U+sip")
                .regexp("!^.*$!sip:admin-service@example.com!").replacement(".")
                .build())
            .build());
    result.put("PTR",
        ptr("ptr-" + rSuffix,
            asList("ptr1-" + dSuffix, "ptr2-" + dSuffix,
                "ptr3-" + dSuffix)));
    result.put("SPF",
        spf("spf-" + rSuffix,
            asList("v=spf1 a -all", "v=spf1 mx -all", "v=spf1 ipv6 -all")));
    result.put("SRV", // designate does not support priority zero!
        ResourceRecordSet.<SRVData>builder().name("_http._tcp" + rSuffix).type("SRV")
            .add(SRVData.builder().priority(1).weight(1).port(80)
                .target("ipv4-" + dSuffix)
                .build())
            .add(SRVData.builder().priority(1).weight(1).port(8080)
                .target("ipv4-" + dSuffix)
                .build())
            .add(SRVData.builder().priority(1).weight(1).port(443)
                .target("ipv4-" + dSuffix)
                .build())
            .build());
    result.put("SSHFP",
        ResourceRecordSet.<SSHFPData>builder().name("ipv4-" + rSuffix).type("SSHFP")
            .add(SSHFPData.createDSA("190E37C5B5DB9A1C455E648A41AF3CC83F99F102"))
            .add(SSHFPData.createDSA("290E37C5B5DB9A1C455E648A41AF3CC83F99F102"))
            .add(SSHFPData.createDSA("390E37C5B5DB9A1C455E648A41AF3CC83F99F102")).build());
    result.put("TXT",
        txt("txt-" + rSuffix,
            asList("made in norway", "made in sweden", "made in finland")));
    return result;
  }

  public static X509Certificate getTestX509Certificate() {
    String x509CertificatePem = emptyToNull(getProperty("discoverydns.x509CertificatePem"));
    if (x509CertificatePem == null) {
      System.err.println("Test certificate not initialised: 'discoverydns.x509CertificatePem' property not found");
      return null;
    }
    try {
      X509CertParser x509CertParser = new X509CertParser();
      x509CertParser.engineInit(new FileInputStream(x509CertificatePem));
      return (X509Certificate) x509CertParser.engineRead();
    } catch (Exception e) {
      System.err.println("Error when initializing test certificate: " + e.getMessage());
      return null;
    }
  }

  public static PrivateKey getTestPrivateKey() {
    String privateKeyPem = emptyToNull(getProperty("discoverydns.privateKeyPem"));
    if (privateKeyPem == null) {
      System.err.println("Test private key not initialised: 'discoverydns.privateKeyPem' property not found");
      return null;
    }
    try {
      PEMParser pemParser = new PEMParser(new FileReader(privateKeyPem));
      final Object o = pemParser.readObject();
      final PrivateKeyInfo privateKeyInfo = ((PEMKeyPair)o).getPrivateKeyInfo();
      KeyFactory keyFact = KeyFactory.getInstance(
          privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId(), new BouncyCastleProvider());
      return keyFact.generatePrivate(new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded()));
    } catch (Exception e) {
      System.err.println("Error when initializing test private key: " + e.getMessage());
      return null;
    }
  }
}
