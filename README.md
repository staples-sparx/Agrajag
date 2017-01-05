# Agrajag

> Agrajag is a creature which, by coincidence, has been killed by Arthur Dent hundreds, maybe thousands of times. He has been reincarnated on multiple occasions, but Arthur Dent is, either directly or indirectly, responsible for his death in some way in every single life he has ever lived. He is first seen notably as a bowl of petunias. At this moment all we know about him are his thoughts: "Oh no, not again". After which many people speculated that if we knew exactly why the bowl of petunias thought this, we would know a lot more of the nature of the Universe than we do now. The reason Agrajag said this is because he was killed by Arthur in many ways before. 

# Daemon that sends repmgr's cluster status to zookeeper

This decouples the DB cluster from the clients or applications. The applications only need to talk to zookeeper to find out the current master.

The daemon runs on the DB nodes, polls repmgr for the current cluster status, and sends this data to zookeeper.

## Usage

- Start this daemon on each of the cluster's nodes that is running postgresql and repmgrd.
- To start this daemon, you could use `bin/svc start repmgr-to-zk`. Feel free to use an alternate way to start this daemon if you prefer.

### Configuration
`repmgr-to-zk` reads its configuration from the `resources/config.edn` file. The sample configuration file `config.edn.sample` can be used as a reference.

- `frequency-ms`: Interval of publishing cluster status to zookeeper in milliseconds.
- `repmgr config-file`: Path to repmgr's config file.
- `zookeer connect`: list of comma separated `host:port`s at which zookeepers are running.
- `zookeeper master-path`: path to the node that contains the ip/hostname of the master in the DB cluster.

## License

Copyright © 2016 Staples Sparx. Released under the MIT license. http://mit-license.org/
