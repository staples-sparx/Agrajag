# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## 0.1.0 - 2016-08-25
### Added
- Parse repmgr's cluster show to get master node's name
- Publish this data to zookeeper at continuous intervals
- Make reading and publishing operations resilient
