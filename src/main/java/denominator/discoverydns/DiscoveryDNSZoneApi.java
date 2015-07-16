package denominator.discoverydns;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import denominator.ZoneApi;
import denominator.model.Zone;
import feign.FeignException;

final class DiscoveryDNSZoneApi implements ZoneApi {
  private static final Pattern zoneNotExistPattern
      = Pattern.compile(".*The zone with id '[0-9a-zA-Z-]{36}' was not found.*", Pattern.DOTALL);

  private final DiscoveryDNS api;
  private final LocalDiscoveryDNSConfiguration localDiscoveryDNSConfiguration;

  DiscoveryDNSZoneApi(DiscoveryDNS api, LocalDiscoveryDNSConfiguration localDiscoveryDNSConfiguration) {
    this.api = api;
    this.localDiscoveryDNSConfiguration = localDiscoveryDNSConfiguration;
  }

  @Override
  public Iterator<Zone> iterator() {
    return getIteratorFromZoneList(api.listZones());
  }

  @Override
  public Iterator<Zone> iterateByName(String name) {
    return getIteratorFromZoneList(api.listZonesByName(name));
  }

  @Override
  public String put(Zone zone) {
    if (zone.id() != null) {
      DiscoveryDNS.Zone ddnsZone = api.getZone(zone.id());
      return updateZone(zone, ddnsZone);
    }
    if (!localDiscoveryDNSConfiguration.isInitialised()) {
      throw new RuntimeException("Local configuration could not be found, so zone creation is not possible");
    }
    try {
      return api.createZone(zone.name(), localDiscoveryDNSConfiguration.getDnssecSigned(),
          localDiscoveryDNSConfiguration.getBrandedNameServers(), localDiscoveryDNSConfiguration.getNameServerSetId(),
          localDiscoveryDNSConfiguration.getPlanId(), localDiscoveryDNSConfiguration.getGroup()).zone.id;
    } catch (FeignException e) {
      if (!e.getMessage().startsWith("status 409 ")
        || !e.getMessage().contains("A zone of that name is already using the associated name server interface set")) {
        throw e;
      }
      return api.listZonesByName(zone.name()).zones.zoneList.iterator().next().id;
    }
  }

  private String updateZone(Zone zone, DiscoveryDNS.Zone ddnsZone) {
    if (!localDiscoveryDNSConfiguration.isInitialised()) {
      throw new RuntimeException("Local configuration could not be found, so zone update is not possible");
    }
    return api.updateZone(zone.id(), ddnsZone.zone.version, localDiscoveryDNSConfiguration.getDnssecSigned(),
        localDiscoveryDNSConfiguration.getBrandedNameServers(), localDiscoveryDNSConfiguration.getNameServerSetId(),
        localDiscoveryDNSConfiguration.getPlanId(), localDiscoveryDNSConfiguration.getGroup()).zone.id;
  }

  @Override
  public void delete(String id) {
    try {
      api.deleteZone(id);
    } catch (FeignException e) {
      if (!isZoneDoesNotExistException(e)) {
        throw e;
      }
    }
  }

  private boolean isZoneDoesNotExistException(FeignException ex) {
    return ex.getMessage().startsWith("status 400 ")
        && (zoneNotExistPattern.matcher(ex.getMessage()).matches()
          || ex.getMessage().contains("The specified string is not a known UUID format"));
  }

  private Iterator<Zone> getIteratorFromZoneList(DiscoveryDNS.Zones zoneList) {
    List<Zone> zones = new LinkedList<Zone>();
    for (DiscoveryDNS.Zones.ZoneList.Zone zone : zoneList.zones.zoneList) {
      zones.add(Zone.create(zone.name, zone.id)); //TODO how to get TTL and email? from ZoneGet?
    }
    return zones.iterator();
  }
}
