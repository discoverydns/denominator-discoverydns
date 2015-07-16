package denominator.discoverydns;

import denominator.DiscoveryDNSTestGraph;
import denominator.Live.UseTestGraph;
import denominator.ReadOnlyLiveTest;

@UseTestGraph(DiscoveryDNSTestGraph.class)
public class DiscoveryDNSReadOnlyLiveTest extends ReadOnlyLiveTest {
  //TODO should expose SOA (in fact all ddns) records for zoneTtlIsEqualToSOATtlProvider?
}
