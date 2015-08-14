# DiscoveryDNS Denominator Provider
This a provider for the [Netflix/denominator](https://github.com/Netflix/denominator) project.
This enables you to use _Denominator_ to connect to the DiscoveryDNS API to manage your zones, along with other DNS providers.

## Homepage
http://www.discoverydns.com/

## API docs
http://discoverydns.github.io/dnsapi-client/

## How to use

### Credentials
The DiscoveryDNS provider expects two credentials to be provided:
* The client certificate in PEM format,
* The non-encrypted client private key in PEM format.

Note: To get a non-encrypted version of the encrypted client private key, use the following command: `openssl rsa -in encrypted.key -out non-encrypted.key`

### As Maven dependency
Include the following dependencies in your Maven project:

    <dependencies>
       <dependency>
          <groupId>com.discoverydns.dnsapi</groupId>
          <artifactId>denominator-discoverydns</artifactId>
          <version>0.0.2</version>
       </dependency>
       <dependency>
          <groupId>com.netflix.denominator</groupId>
          <artifactId>denominator-core</artifactId>
          <version>4.7.0</version>
       </dependency>
    </dependencies>

### As Gradle dependency
Include the following dependencies in your Gradle project:

    dependencies {
       compile 'com.discoverydns.dnsapi:denominator-discoverydns:0.0.2'
       compile 'com.netflix.denominator:denominator-core:4.7.0'
    }

### In the code
Then you can create your DiscoveryDNS Provider as:

    DNSApiManager manager = Denominator.create("discoverydns", credentials(certificate, privateKey));

For more information, see [here](https://github.com/Netflix/denominator#third-party-providers).

### As a CLI provider
You can use DiscoveryDNS as a CLI provider by downloading
 the [denominator-discoverydns jar](https://github.com/discoverydns/denominator-discoverydns/tree/master/dist/denominator-discoverydns-0.0.1.jar)
 and following [these instructions](https://github.com/Netflix/denominator/tree/master/cli#third-party-providers).

You can then use the following configuration in the YAML configuration file:

```
name: discoverydns-prod
provider: discoverydns
credentials:
 x509Certificate:
 |
  -----BEGIN CERTIFICATE-----
  [PEM CONTENT HERE]
  -----END CERTIFICATE-----
 privateKey:
 |
  -----BEGIN PRIVATE KEY-----
  [PEM CONTENT HERE]
  -----END PRIVATE KEY-----
```

Then use the `-n` arg to select the named provider.

For example, `./denominator -n discoverydns-prod zone`

Or with environment variables:
```
export DENOMINATOR_PROVIDER=discoverydns
export DENOMINATOR_URL=https://alternative/rest/endpoint
export DENOMINATOR_X509_CERTIFICATE=`-----BEGIN CERTIFICATE-----\n[PEM CONTENT HERE]\n-----END CERTIFICATE-----`
export DENOMINATOR_PRIVATE_KEY=`-----BEGIN PRIVATE KEY-----\n[PEM CONTENT HERE]\n-----END PRIVATE KEY-----`
```

### Additional configuration for Zone Create/Update
To use the Zone Create and Zone Update operations,
 some additional parameters need to be passed to the DiscoveryDNS Denominator Provider:
* The id of the nameServerSet to use for the zone, to configure the proper nameServer names.
* The id of the plan to use for the zone, to configure the usable functionalities and the allowed usage.
* An indication if the zone should use the branded nameServers functionality (if the chosen plan enables it).
* An indication if the zone should use the DNSSEC functionality (if the chosen plan enables it).
* The name of the group to put the zone in (if the chosen plan enables grouping).
This is done either programmatically by setting the local thread configuration on the Provider:

```
discoveryDNSProvider.getLocalConfiguration().setNameServerSetId("myNameServerSetId");
discoveryDNSProvider.getLocalConfiguration().setPlanId("myPlanId");
discoveryDNSProvider.getLocalConfiguration().setBrandedNameServers(true);
discoveryDNSProvider.getLocalConfiguration().setDnssecSigned(false);
discoveryDNSProvider.getLocalConfiguration().setGroup("myGroup");
String zoneId = manager.api().zones().put(Zone.create("my-zone-name.com", null));
```

Or (especially for the CLI), by a ".discoverydnsconfig" configuration file,
 which values will then be used for all subsequent calls to the Zone Create or Update operations:

```
nameServerSetId: myNameServerSetId
planId: myPlanId
brandedNameServers: true
dnssecSigned: false
group: myGroup
```

This file will be looked for, during startup, in the following folders:
* .
* ./3rdparty/
* ~
