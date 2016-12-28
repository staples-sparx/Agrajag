### dealing with stale masters

* query repmgr's repl_events table to find the timestamp of the lastest master promotion
* if the timestamp from any of the other nodes is older than the one found, write that one and reject anything else
