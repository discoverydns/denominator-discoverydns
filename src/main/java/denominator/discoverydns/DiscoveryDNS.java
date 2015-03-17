package denominator.discoverydns;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import denominator.model.ResourceRecordSet;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({"Accept: application/json", "Content-Type: application/json"})
interface DiscoveryDNS {

  @RequestLine("GET /users")
  Users listUsers();

  @RequestLine("GET /zones")
  Zones listZones();

  @RequestLine("GET /zones/{id}")
  Zone getZone(@Param("id") String id);

  @RequestLine("PUT /zones/{id}/resourcerecords")
  void updateZone(@Param("id") String id, Zone zone);

  static final class ResourceRecords {

    List<ResourceRecordSet<?>> records = new ArrayList<ResourceRecordSet<?>>();
  }

  static final class Zones {

    ZoneList zones;

    class ZoneList {

      List<Zone> zoneList;

      class Zone {

        String id;
        String name;
      }
    }
  }

  static final class Zone {

    ZoneData zone;

    ZoneData zoneUpdateResourceRecords;

    class ZoneData {

      String id;
      Long version;

      ResourceRecords resourceRecords;
    }
  }

  static final class RecordSetDetails {
    String name;
    String type;
    Integer ttl;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((ttl == null) ? 0 : ttl.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null && obj.getClass().equals(this.getClass()) && hashCode() == obj.hashCode();
    }
  }

  static final class Record {
    RecordSetDetails recordSetDetails = new RecordSetDetails();
    Map<String, Object> rDataValues = new LinkedHashMap<String, Object>();

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((recordSetDetails == null) ? 0 : recordSetDetails.hashCode());
      result = prime * result + ((rDataValues == null) ? 0 : rDataValues.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      return obj != null && obj.getClass().equals(this.getClass()) && hashCode() == obj.hashCode();
    }
  }

  static final class Users {

    UserList users;

    class UserList {

      List<User> userList;

      class User {
        String id;
      }
    }
  }
}
