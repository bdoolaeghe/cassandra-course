TP3 - FAULT TOLERANCE
=====================

***TP3 will illustrate the fault tolerance cassandra capabilities***

Installation
------------
Let's install and start a 3-node cassandra cluster (using docker-compose)...

### Install scope


[Weave scope](https://www.weave.works/oss/scope/) will help to manage your cassandra cluster docker containers.

* download and start weave scope:
```
curl -L git.io/scope -o ~/bin/scope &&
chmod +x ~/bin/scope &&
~/bin/scope launch
```

* [open](http://localhost:4040) scope in a web browser.

### Install a 3-node cassandra cluster

A 3-node cassandra cluster is already configured in [docker-compose.yml](docker-compose.yml)

* startup the cluster with docker-compose:
```
cd TPS/TP3/
docker-compose up -d
```

_NB: you can check logs with:_
```
docker-compose logs -f
```
Once the cluster is up, you should [see the 3 connected containers on weave scope](http://localhost:4040). Now, you'll need in the next steps to open a terminal on node containers (click on a node to select) and execute a cqlsh with button ">\_" (Execute shell). For convinience, the TPs sources are mounted in _/TPs/_ in each node container. 


[>> Next (TP3.1_masterless_archi.md](TP3.1_masterless_archi.md)
