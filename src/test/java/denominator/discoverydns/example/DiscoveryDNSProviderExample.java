package denominator.discoverydns.example;

import denominator.*;
import denominator.discoverydns.DiscoveryDNSProvider;
import denominator.model.ResourceRecordSet;
import denominator.model.Zone;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static denominator.CredentialsConfiguration.credentials;
import static feign.Util.emptyToNull;
import static java.lang.System.getProperty;

public class DiscoveryDNSProviderExample {
  public static void main(String[] args) {
    DiscoveryDNSProvider discoveryDNSProvider = new DiscoveryDNSProvider(emptyToNull(getProperty("discoverydns.url")));
    DNSApiManager manager = Denominator.create(discoveryDNSProvider,
        credentials(DiscoveryDNSTestGraph.getTestX509Certificate(), DiscoveryDNSTestGraph.getTestPrivateKey()));

    //List zones
    final ZoneApi zones = manager.api().zones();
    for (Zone next : zones) {
      System.out.println(next.name());
    }

    //Create zone
    discoveryDNSProvider.getLocalConfiguration().setBrandedNameServers(true);
    discoveryDNSProvider.getLocalConfiguration().setDnssecSigned(false);
    discoveryDNSProvider.getLocalConfiguration().setNameServerSetId("<putNameServerSetIdHere>");
    discoveryDNSProvider.getLocalConfiguration().setPlanId("<putPlanIdHere>");
    discoveryDNSProvider.getLocalConfiguration().setGroup("<putGroupNameHere>");
    String zoneName = "test-zone" + new Date().getTime() + ".com";
    String zoneId = manager.api().zones().put(Zone.create(zoneName, null));
    System.out.println("==== Zone " + zoneName + " of id " + zoneId + " created");

    //Add records
    Map<String, Object> record = new HashMap<String, Object>();
    record.put("address", "5.4.3.2");
    final ResourceRecordSet<Map<String, Object>> rrSet
        = ResourceRecordSet.builder().name("test." + zoneName + ".").type("A").ttl(9000).add(record).build();
    manager.api().recordSetsInZone(zoneId).put(rrSet);

    //Read zone records
    final AllProfileResourceRecordSetApi resourceRecordSets = manager.api().recordSetsInZone(zoneId);
    for (ResourceRecordSet<?> resourceRecordSet : resourceRecordSets) {
      System.out.println(resourceRecordSet);
    }

    //Delete records
    manager.api().recordSetsInZone(zoneId).deleteByNameAndType("test." + zoneName + ".", "A");

    //Update zone
    discoveryDNSProvider.getLocalConfiguration().setBrandedNameServers(false);
    discoveryDNSProvider.getLocalConfiguration().setGroup("test2");
    manager.api().zones().put(Zone.create(zoneName, zoneId));
    System.out.println("==== Zone " + zoneName + " of id " + zoneId + " updated");

    //Delete non-existing zone should not throw exception
    manager.api().zones().delete("unknown");
    manager.api().zones().delete(UUID.randomUUID().toString());

    //Delete existing zone
    manager.api().zones().delete(zoneId);
  }
}
