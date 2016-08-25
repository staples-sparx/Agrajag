# repmgr-to-zk

A daemon that sends repmgr's cluster status to zookeeper.

This decouples the DB cluster from the clients or applications. The applications only need to talk to zookeeper to find out the current master.

The daemon runs on the DB nodes, polls repmgr for the current cluster status, and sends this data to zookeeper.

## Usage

- Start this daemon on each of the cluster's nodes that is running postgresql and repmgrd.
- To start this daemon, you could use `bin/svc start repmgr-to-zk`. Feel free to use an alternate way to start this daemon if you prefer.

### Configuration
- `frequency-ms`: Interval of publishing cluster status to zookeeper in milliseconds.
- `repmgr config-file`: Path to repmgr's config file.
- `zookeer connect`: list of comma separated `host:port`s at which zookeepers are running.
- `zookeeper master-path`: path to the node that contains the ip/hostname of the master in the DB cluster.

## License

Copyright © 2016 Staples Sparx. Released under the MIT license. http://mit-license.org/
