# DiscoveryDNS Denominator Provider
This a provider for the [Netflix/denominator](https://github.com/Netflix/denominator) project.
This enables you to use _Denominator_ to connect to the DiscoveryDNS API to manage your zones, along with other DNS providers.

## Homepage
http://www.discoverydns.com/

## API docs
http://discoverydns.github.io/dnsapi-client/

## How to use

### As Maven dependency
Include the following dependencies in your Maven project
<dependencies>
   <dependency>
      <groupId>com.discoverydns.dnsapi</groupId>
      <artifactId>denominator-discoverydns</artifactId>
      <version>0.0.1</version>
   </dependency>
   <dependency>
      <groupId>com.netflix.denominator</groupId>
      <artifactId>denominator-core</artifactId>
      <version>8.1.0</version>
   </dependency>
   <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-core</artifactId>
      <version>0.0.1</version>
   </dependency>
   <dependency>
      <groupId>com.netflix.feign</groupId>
      <artifactId>feign-gson</artifactId>
      <version>8.1.0</version>
   </dependency>
</dependencies>

### As Gradle dependency
Include the following dependencies in your Gradle project:
dependencies {
  compile 'com.discoverydns.dnsapi:denominator-discoverydns:0.0.1'
  compile 'com.netflix.denominator:denominator-core:4.4.2'
  compile 'com.netflix.feign:feign-core:8.1.0'
  compile 'com.netflix.feign:feign-gson:8.1.0'
}
Then you can create your DiscoveryDNS Provider as:
DNSApiManager manager = Denominator.create("discoverydns", credentials(certificate, privateKey));

### As a CLI provider
TODO