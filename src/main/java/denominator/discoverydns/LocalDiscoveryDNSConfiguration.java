package denominator.discoverydns;

import com.google.common.collect.Lists;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map;

public class LocalDiscoveryDNSConfiguration {
  private ThreadLocal<Configuration> localConfiguration = new ThreadLocal<Configuration>();
  private Configuration defaultConfiguration = new Configuration();

  public LocalDiscoveryDNSConfiguration() {
    attemptLoadingFromConfigFile();
  }

  private void attemptLoadingFromConfigFile() {
    try {
      final Map configMap = Map.class.cast(new Yaml().loadAll(
          new FileInputStream(getConfigFile())).iterator().next());
      defaultConfiguration.dnssecSigned = Boolean.parseBoolean(configMap.get("dnssecSigned").toString());
      defaultConfiguration.brandedNameServers = Boolean.parseBoolean(configMap.get("brandedNameServers").toString());
      defaultConfiguration.planId = configMap.get("planId").toString();
      defaultConfiguration.group = configMap.get("group").toString();
      defaultConfiguration.nameServerSetId = configMap.get("nameServerSetId").toString();
    } catch (Exception e) {
      //Ignore
    }
  }

  private File getConfigFile() {
    final ArrayList<String> configFilePaths = Lists.newArrayList("3rdparty/.discoverydnsconfig",
        ".discoverydnsconfig", System.getProperty("user.home") + "/.discoverydnsconfig");
    for (String configFilePath : configFilePaths) {
      File configFile = new File(configFilePath);
      if (configFile.exists() && configFile.canRead()) {
        return configFile;
      }
    }
    throw new RuntimeException("Configuration file not found");
  }

  private Configuration getLocalConfiguration() {
    if (!isInitialised()) {
      localConfiguration.set(new Configuration(defaultConfiguration));
    }
    return localConfiguration.get();
  }

  public boolean isInitialised() {
    return localConfiguration.get() != null;
  }

  private static class Configuration {
    private Boolean dnssecSigned;
    private Boolean brandedNameServers;
    private String planId;
    private String group;
    private String nameServerSetId;

    private Configuration() {
    }

    private Configuration(Configuration defaultConfiguration) {
      this.dnssecSigned = defaultConfiguration.dnssecSigned;
      this.brandedNameServers = defaultConfiguration.brandedNameServers;
      this.planId = defaultConfiguration.planId;
      this.group = defaultConfiguration.group;
      this.nameServerSetId = defaultConfiguration.nameServerSetId;
    }
  }

  public Boolean getDnssecSigned() {
    return getLocalConfiguration().dnssecSigned;
  }

  public void setDnssecSigned(Boolean dnssecSigned) {
    getLocalConfiguration().dnssecSigned = dnssecSigned;
  }

  public Boolean getBrandedNameServers() {
    return getLocalConfiguration().brandedNameServers;
  }

  public void setBrandedNameServers(Boolean brandedNameServers) {
    getLocalConfiguration().brandedNameServers = brandedNameServers;
  }

  public String getPlanId() {
    return getLocalConfiguration().planId;
  }

  public void setPlanId(String planId) {
    getLocalConfiguration().planId = planId;
  }

  public String getGroup() {
    return getLocalConfiguration().group;
  }

  public void setGroup(String group) {
    getLocalConfiguration().group = group;
  }

  public String getNameServerSetId() {
    return getLocalConfiguration().nameServerSetId;
  }

  public void setNameServerSetId(String nameServerSetId) {
    getLocalConfiguration().nameServerSetId = nameServerSetId;
  }

  public void useDefaultConfiguration() {
    getLocalConfiguration();
  }
}
