### why use repmgr as the clustering / failover tool?

* because it's written in C: has a low-level access to your db cluster and logs everything in your primary database, it's fast and tightly coupled to your db
* it's old and battle-tested and written by folks who are core committers to postgres itself

### why write Agrajag in Clojure?

* because it gives strong monitoring, great libraries and good code safety primitives

### if it spins off a JVM doesn't this make agrajag resource heavy?

* it does, we're working on it to heavily optimize it to take a tiny amount of heap and less CPU

### why run agrajag as a daemon?

* because it helps monitor the heartbeat of the current master db much easier than a one-time hook
* because it helps monitor the process's (the process that will eventually write to ZK or post to your application or PgBouncer or wherever) health continually than a one-off script failing to start the process at the time of the failover (due to any unknown underlying change) 
* if zk (or anything other underlying dependencies) fails, you will be notified early

## why not call application / db bouncer directly?
* because if you have a lot of standbys, then your `standby_promote` / `follow_master` script becomes a complex graph of requests and retries
* calling it directly will involve hitting each of your application nodes multiple times which is uncessary, instead we can just read from a clustered zk on a timely basis from the app side
* if you call API endpoints, your app will have to manage cluster state
* it's also a better idea to keep the `standby_promote` / `follow_master` hook as simple as possible and have it work as little as possible
* since `cluster_show` already gives you the current live picture of the cluster per node, it makes sense to just query it and write it to a central location (like ZK) from where you can later read

## why not call zk directly from repmgr automatic failover scripts?
* we have some basic requirements for this script, it should at least do the following:
  * good monitoring and heartbeat functionality
  * being able to retry if it fails
  * handle I/O exceptions
  * boot-up fast (we don't want to waste time during this process)
* most scripting languages like Python / Ruby will provide these functionalities, but we've chosen Clojure as a preferred language for this for the reasons mentioned in a question above, we do however understand that:
  * the JVM is slow to boot up, it doesn't make sense for it to be a script
  * it's also one of the reasons why agrajag is designed as a daemon / orchestrator rather than a script other than the other benefits of keeping it as a daemon/service mentioned in the question above

### why not run agrajag after the repmgr master_register event?

* because the events are a passive audit log and not necessarily reliable
* we use a combination of `cluster show` and `repl_events` to write the master which is more foolproof
