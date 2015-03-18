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
          <version>0.0.1</version>
       </dependency>
       <dependency>
          <groupId>com.netflix.denominator</groupId>
          <artifactId>denominator-core</artifactId>
          <version>8.1.0</version>
       </dependency>
    </dependencies>

### As Gradle dependency
Include the following dependencies in your Gradle project:

    dependencies {
       compile 'com.discoverydns.dnsapi:denominator-discoverydns:0.0.1'
       compile 'com.netflix.denominator:denominator-core:4.4.2'
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
 certificatePem: |
 -----BEGIN CERTIFICATE-----
 [PEM CONTENT HERE]
 -----END CERTIFICATE-----
 keyPem: |
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
export DENOMINATOR_X509_CERTIFICATE=-----BEGIN CERTIFICATE-----[PEM CONTENT HERE]-----END CERTIFICATE-----
export DENOMINATOR_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----[PEM CONTENT HERE]-----END PRIVATE KEY-----
```