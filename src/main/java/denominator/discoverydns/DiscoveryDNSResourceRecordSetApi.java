package denominator.discoverydns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import denominator.ResourceRecordSetApi;
import denominator.model.ResourceRecordSet;

import static denominator.common.Util.filter;
import static denominator.common.Util.nextOrNull;
import static denominator.model.ResourceRecordSets.nameAndTypeEqualTo;
import static denominator.model.ResourceRecordSets.nameEqualTo;

final class DiscoveryDNSResourceRecordSetApi implements ResourceRecordSetApi {

  private String zoneId;
  private DiscoveryDNS api;

  DiscoveryDNSResourceRecordSetApi(String zoneId, DiscoveryDNS api) {
    this.zoneId = zoneId;
    this.api = api;
  }

  @Override
  public Iterator<ResourceRecordSet<?>> iterator() {
    List<ResourceRecordSet<?>> records = api.getZone(zoneId).zone.resourceRecords.records;
    return records.iterator();
  }

  @Override
  public Iterator<ResourceRecordSet<?>> iterateByName(String name) {
    List<ResourceRecordSet<?>> records = api.getZone(zoneId).zone.resourceRecords.records;
    return filter(records.iterator(), nameEqualTo(appendDotToRecordNameIfNecessary(name)));
  }

  @Override
  public ResourceRecordSet<?> getByNameAndType(String name, String type) {
    List<ResourceRecordSet<?>> records = api.getZone(zoneId).zone.resourceRecords.records;
    return nextOrNull(filter(records.iterator(), nameAndTypeEqualTo(appendDotToRecordNameIfNecessary(name), type)));
  }

  private void updateResourceRecords(String zoneId, String name, String type,
                                     ResourceRecordSet<?>... appends) {
    DiscoveryDNS.Zone ddnsZone = api.getZone(zoneId);
    List<ResourceRecordSet<?>> records = new ArrayList<ResourceRecordSet<?>>();
    for (ResourceRecordSet<?> record : ddnsZone.zone.resourceRecords.records) {
      if (!appendDotToRecordNameIfNecessary(name).equals(record.name()) || !type.equals(record.type())
          && (appends.length == 0 || !appends[0].ttl().equals(record.ttl()))) {
        records.add(record);
      }
    }
    if (appends != null) {
      Collections.addAll(records, appends);
    }

    DiscoveryDNS.Zone ddnsUpdateZone = new DiscoveryDNS.Zone();
    ddnsUpdateZone.zoneUpdateResourceRecords = ddnsZone.zone;
    ddnsUpdateZone.zoneUpdateResourceRecords.resourceRecords.records = records;
    api.updateZoneRecords(zoneId, ddnsUpdateZone);
  }

  @Override
  public void put(ResourceRecordSet<?> rrset) {
    updateResourceRecords(zoneId, rrset.name(), rrset.type(), rrset);
  }

  @Override
  public void deleteByNameAndType(String name, String type) {
    updateResourceRecords(zoneId, name, type);
  }

  private String appendDotToRecordNameIfNecessary(String recordName) {
    return recordName == null ? null : (recordName.endsWith(".") ? recordName : recordName + ".");
  }

  static final class Factory implements denominator.ResourceRecordSetApi.Factory {

    private final DiscoveryDNS api;

    Factory(DiscoveryDNS api) {
      this.api = api;
    }

    @Override
    public ResourceRecordSetApi create(String zoneId) {
      return new DiscoveryDNSResourceRecordSetApi(zoneId, api);
    }
  }
}
