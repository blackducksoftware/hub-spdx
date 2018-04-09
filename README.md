# hub-spdx
The Software Package Data Exchange® (SPDX®) specification (https://spdx.org) is a standard format for communicating the components, licenses and copyrights associated with software packages. Software Suppliers sometimes use SPDX format to communicate the list of open source components contained in the software they are supplying. 

hub-spdx is a utility that generates an SPDX report (RDF format) representing the Bill Of Materials (BOM) of a Black Duck Hub project version.

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
--include.licenses=true
    # If true, include license info for each component
    # default: false
--use.spdx.org.license.data=false
    # If true: fetch license text from spdx.org when possible; see https://spdx.org/licenses/
    # default: true
--hub.always.trust.cert=true
    # If true, trust any Hub server certificate
    # default: false
--logging.level.com.blackducksoftware=<logging level>
    # Logging levels: INFO, DEBUG, TRACE
```

### Optional environment variables:
```
export BD_HUB_PASSWORD=<Hub password>
export BD_HUB_PROXY_PASSWORD=<proxy password>
```

## Other SPDX Tools
If you a need a utility to validate and view SPDX files, convert RDF format to HTML, etc., please see: https://github.com/spdx/tools/
