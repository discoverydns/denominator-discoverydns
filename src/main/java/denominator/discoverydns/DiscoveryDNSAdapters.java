package denominator.discoverydns;

import static denominator.discoverydns.DiscoveryDNSFunctions.toRDataMap;
import static denominator.discoverydns.DiscoveryDNSFunctions.toRecord;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import denominator.common.Util;
import denominator.discoverydns.DiscoveryDNS.ResourceRecords;
import denominator.model.ResourceRecordSet;

final class DiscoveryDNSAdapters {

  static final class ResourceRecordsAdapter extends TypeAdapter<ResourceRecords> {

    @Override
    public void write(JsonWriter jsonWriter, ResourceRecords records)
        throws IOException {
      jsonWriter.beginArray();
      for (ResourceRecordSet<?> rrset : records.records) {
        for (Map<String, Object> rdata : rrset.records()) {
          jsonWriter.beginObject();
          jsonWriter.name("name").value(rrset.name());
          jsonWriter.name("class").value("IN");
          jsonWriter.name("ttl").value(rrset.ttl() == null ? "3600"
                                                           : rrset.ttl().toString());
          jsonWriter.name("type").value(rrset.type());
          jsonWriter.name("rdata").value(Util.flatten(rdata));
          jsonWriter.endObject();
        }
      }
      jsonWriter.endArray();
    }

    @Override
    public ResourceRecords read(JsonReader in) throws IOException {
      Map<DiscoveryDNS.RecordSetDetails, Collection<DiscoveryDNS.Record>> rrsets
          = new LinkedHashMap<DiscoveryDNS.RecordSetDetails, Collection<DiscoveryDNS.Record>>();
      in.beginArray();
      while (in.hasNext()) {
        DiscoveryDNS.Record record = toRecord(in);
        if (!rrsets.containsKey(record.recordSetDetails)) {
          rrsets.put(record.recordSetDetails, new ArrayList<DiscoveryDNS.Record>());
        }
        rrsets.get(record.recordSetDetails).add(record);
      }
      in.endArray();

      DiscoveryDNS.ResourceRecords ddnsRecords = new DiscoveryDNS.ResourceRecords();
      for (Map.Entry<DiscoveryDNS.RecordSetDetails, Collection<DiscoveryDNS.Record>> entry : rrsets.entrySet()) {
        DiscoveryDNS.RecordSetDetails rrSetDetails = entry.getKey();
        ResourceRecordSet.Builder<Map<String, Object>> builder = ResourceRecordSet.builder()
            .name(rrSetDetails.name)
            .type(rrSetDetails.type)
            .ttl(rrSetDetails.ttl);
        for (DiscoveryDNS.Record record : entry.getValue()) {
          builder.add(toRDataMap(record));
        }
        ddnsRecords.records.add(builder.build());
      }
      return ddnsRecords;
    }
  }
}
