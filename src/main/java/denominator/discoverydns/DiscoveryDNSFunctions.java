package denominator.discoverydns;

import static denominator.common.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import denominator.model.rdata.AAAAData;
import denominator.model.rdata.AData;
import denominator.model.rdata.CERTData;
import denominator.model.rdata.CNAMEData;
import denominator.model.rdata.MXData;
import denominator.model.rdata.NAPTRData;
import denominator.model.rdata.NSData;
import denominator.model.rdata.PTRData;
import denominator.model.rdata.SPFData;
import denominator.model.rdata.SRVData;
import denominator.model.rdata.SSHFPData;
import denominator.model.rdata.TXTData;

public final class DiscoveryDNSFunctions {
  private DiscoveryDNSFunctions() {
  }

  static DiscoveryDNS.Record toRecord(JsonReader in) throws IOException {
    in.beginObject();
    DiscoveryDNS.Record record = new DiscoveryDNS.Record();
    while (in.hasNext()) {
      String name = in.nextName();
      if ("name".equals(name)) {
        record.recordSetDetails.name = in.nextString();
      } else if ("type".equals(name)) {
        record.recordSetDetails.type = in.nextString();
      } else if ("ttl".equals(name)) {
        record.recordSetDetails.ttl = in.nextInt();
      } else if ("class".equals(name)) {
        in.skipValue();
      } else {
        if ("strings".equals(name)) {
          try {
            in.beginArray();
            List<String> strings = new ArrayList<String>();
            while (in.hasNext()) {
              strings.add(in.nextString());
            }
            in.endArray();
            record.rDataValues.put(name, strings.toArray(new String[strings.size()]));
          } catch (IllegalStateException ex) {
            record.rDataValues.put(name, in.nextString());
          }
        } else {
          JsonToken token = in.peek();
          if (token == JsonToken.NULL) {
            in.nextNull();
          } else {
            record.rDataValues.put(name, in.nextString());
          }
        }
      }
    }
    in.endObject();
    return record;
  }

  static Map<String, Object> toRDataMap(DiscoveryDNS.Record record) {
    if ("A".equals(record.recordSetDetails.type)) {
      return AData.create((String) record.rDataValues.get("address"));
    } else if ("NS".equals(record.recordSetDetails.type)) {
      return NSData.create((String) record.rDataValues.get("target"));
    } else if ("CNAME".equals(record.recordSetDetails.type)) {
      return CNAMEData.create((String) record.rDataValues.get("target"));
    } else if ("PTR".equals(record.recordSetDetails.type)) {
      return PTRData.create((String) record.rDataValues.get("target"));
    } else if ("MX".equals(record.recordSetDetails.type)) {
      return MXData.create(Integer.parseInt((String) record.rDataValues.get("priority")),
          (String) record.rDataValues.get("target"));
    } else if ("TXT".equals(record.recordSetDetails.type)) {
      return TXTData.create(concatenateMultipleStrings(record.rDataValues.get("strings")));
    } else if ("AAAA".equals(record.recordSetDetails.type)) {
      final Object address = record.rDataValues.get("address");
      checkNotNull(address, "address");
      return AAAAData.create(((String) address).toUpperCase());
    } else if ("SRV".equals(record.recordSetDetails.type)) {
      return new SRVData.Builder()
          .priority(Integer.parseInt((String) record.rDataValues.get("priority")))
          .weight(Integer.parseInt((String) record.rDataValues.get("weight")))
          .port(Integer.parseInt((String) record.rDataValues.get("port")))
          .target((String) record.rDataValues.get("target"))
          .build();
    } else if ("NAPTR".equals(record.recordSetDetails.type)) {
      return new NAPTRData.Builder()
          .order(Integer.parseInt((String) record.rDataValues.get("order")))
          .preference(Integer.parseInt((String) record.rDataValues.get("preference")))
          .flags((String) record.rDataValues.get("flags"))
          .services((String) record.rDataValues.get("service"))
          .regexp((String) record.rDataValues.get("regexp"))
          .replacement((String) record.rDataValues.get("replacement"))
          .build();
    } else if ("CERT".equals(record.recordSetDetails.type)) {
      return new CERTData.Builder()
          .format(Integer.parseInt((String) record.rDataValues.get("certType")))
          .tag(Integer.parseInt((String) record.rDataValues.get("keyTag")))
          .algorithm(Integer.parseInt((String) record.rDataValues.get("algorithm")))
          .certificate((String) record.rDataValues.get("cert"))
          .build();
    } else if ("SSHFP".equals(record.recordSetDetails.type)) {
      return new SSHFPData.Builder()
          .algorithm(Integer.parseInt((String) record.rDataValues.get("algorithm")))
          .fptype(Integer.parseInt((String) record.rDataValues.get("digestType")))
          .fingerprint((String) record.rDataValues.get("fingerprint"))
          .build();
    } else if ("SPF".equals(record.recordSetDetails.type)) {
      return SPFData.create(concatenateMultipleStrings(record.rDataValues.get("strings")));
    } else {
      return record.rDataValues;
    }
  }

  private static String concatenateMultipleStrings(Object strings) {
    if (strings instanceof String[]) {
      String concatenatedStrings = "";
      for (int i = 0; i < ((String[]) strings).length ; i++) {
        if (i > 0) {
          concatenatedStrings += " ";
        }
        concatenatedStrings += "\"" + ((String[]) strings)[i] + "\"";
      }
      return concatenatedStrings;
    } else {
      return (String) strings;
    }
  }
}
