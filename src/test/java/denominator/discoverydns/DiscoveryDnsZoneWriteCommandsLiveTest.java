package denominator.discoverydns;

import denominator.DiscoveryDNSTestGraph;
import denominator.Live;
import denominator.ZoneWriteCommandsLiveTest;

@Live.UseTestGraph(DiscoveryDNSTestGraph.class)
public class DiscoveryDnsZoneWriteCommandsLiveTest extends ZoneWriteCommandsLiveTest {
  //test1_putNewZone, test3_putChangingDefaultTTL & test4_putChangingEmail cannot pass, as zone ttl and email cannot be set from API
  //TODO should expose SOA (in fact all ddns) records for test2_zoneTtlIsEqualToSOATtl and test4_zoneTtlIsEqualToSOATtl?
  //test5_deleteZoneWhenNotEmpty cannot pass, as user cannot add top-level NS records
}
