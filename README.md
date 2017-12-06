# hub-spdx
hub-spdx is a utility that generates an SPDX report representing the Bill Of Materials (BOM) of a Black Duck Hub project version.

## Prerequisites

Java 1.8

## Example usage:
```
java -jar hub-spdx-<version>.jar \
--hub.url=<Hub URL> \
--hub.username=<Hub username> \
--hub.password=<Hub password> \
--hub.project.name=<Hub project name> \
--hub.project.version=<Hub project version> \
--output.filename=<path to report file> \
--include.licenses=true
```

### Required arguments:
```
--hub.url=<Hub URL>
--hub.username=<Hub username>
--hub.project.name=<Hub project name>
--hub.project.version=<Hub project version>
--output.filename=<path to report file>
```
  
### Optional arguments:
```
--hub.timeout=<Hub timeout in seconds>
--hub.password=<Hub password>
--hub.proxy.host=<proxy host>
--hub.proxy.port=<proxy port>
--hub.proxy.username=<proxy username>
--hub.proxy.password=<proxy password>
--include.licenses=true # Include license info for each component
--hub.always.trust.cert=true # Trust any Hub server certificate
--logging.level.com.blackducksoftware=<logging level> # INFO, DEBUG, TRACE
```

### Optional environment variables:
```
export BD_HUB_PASSWORD=<Hub password>
export BD_HUB_PROXY_PASSWORD=<proxy password>
```

## Limitations

* The SPDX report produced is has minimal details.
* There is no support for proxies.

## Other SPDX Tools
If you a need a utility to validate and view SPDX files, you can get one here: https://github.com/spdx/tools/releases
