# Overview
The Software Package Data Exchange® (SPDX®) specification (https://spdx.org) is a standard format for communicating the components and licenses associated with software packages. Software Suppliers sometimes use SPDX format to communicate the list of open source components contained in the software they are supplying. 

hub-spdx is a utility that generates an SPDX report (RDF format) representing the Bill Of Materials (BOM) of a Black Duck Hub project version.

# Build

[![Build Status](https://travis-ci.org/blackducksoftware/hub-spdx.svg?branch=master)](https://travis-ci.org/blackducksoftware/hub-spdx)
[![Coverage Status](https://coveralls.io/repos/github/blackducksoftware/hub-spdx/badge.svg?branch=master)](https://coveralls.io/github/blackducksoftware/hub-spdx?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Black Duck Security Risk](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/hub-spdx/branches/master/badge-risk.svg)](https://copilot.blackducksoftware.com/github/repos/blackducksoftware/hub-spdx/branches/master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.blackducksoftware.integration%3Ahub-spdx&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.blackducksoftware.integration%3Ahub-spdx)

# Prerequisites

Java 1.8

# Example usage:
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

## Required arguments:
```
--hub.url=<Hub URL>
--hub.username=<Hub username>
--hub.project.name=<Hub project name>
--hub.project.version=<Hub project version>
--output.filename=<path to report file>
```
  
## Optional arguments:
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

## Optional environment variables:
```
export BD_HUB_PASSWORD=<Hub password>
export BD_HUB_PROXY_PASSWORD=<proxy password>
```

## Other SPDX Tools
If you a need a utility to validate and view SPDX files, convert RDF format to HTML, etc., please see: https://github.com/spdx/tools/
