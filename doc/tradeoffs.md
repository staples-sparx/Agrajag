### why use repmgr as the clustering / failover tool?

* because it's written in C: has a low-level access to your db cluster and logs everything in your primary database, it's fast and tightly coupled to your db
* it's old and battle-tested and written by folks who are core committers to postgres itself

### why write Agrajag in Clojure?

* because it gives strong monitoring, great libraries and good code safety primitives

### if it spins off a JVM doesn't this make agrajag resource heavy?

* it does, we're working on it to heavily optimize it to take a tiny amount of heap and less CPU

### why run agrajag as a daemon?

* because it helps monitor the heartbeat of the current master much easier than a one-time hook
* it's more reliable in terms of master consensus since multiple of these will be running on each box

### why not run agrajag as a repmgr standXSby_promote hook?

* because that will only run on the master box and if for some reason the call to agrajag fails, we're in trouble
* a better alternative is to run it everywhere and use `cluster show` which will resolve the master for us and give us a better chance of writing the master
* it's also a better idea to keep the standby_promote hook as simple as possible and have it work as little as possible

### why not run agrajag after the repmgr master_register event?

* because the events are a passive audit log and not necessarily reliable
* we use a combination of `cluster show` and `repl_events` to write the master which is more foolproof
