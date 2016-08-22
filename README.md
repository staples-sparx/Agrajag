# repmgr-to-zk

A daemon that sends repmgr's cluster status to zookeeper.

## Usage

This decouples the DB cluster from the clients or applications. The applications only need to talk to zookeeper to find out the current master.

The daemon runs on the DB nodes, polls repmgr for the current cluster status, and sends this data to zookeeper.

## License

Copyright © 2016 staples-sparx.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
