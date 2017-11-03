# hub-spdx
hub-spdx is a utility that generates an SPDX report representing the Bill Of Materials (BOM) of a Hub project version.

## Usage
```
java -jar build/libs/hub-spdx-<version>.jar \
--hub.url=<Hub URL> \
--hub.username=<Hub username> \
--hub.password=<Hub password> \
--hub.project.name=<Hub project name> \
--hub.project.version=<Hub project version> \
--output.filename=<path to report file>
```

### Optional arguments:
--hub.always.trust.cert=true # Trust any Hub server certificate

## Other SPDX Tools
If you a need a utility to validate and view SPDX files, you can get one here: https://github.com/spdx/tools/releases
