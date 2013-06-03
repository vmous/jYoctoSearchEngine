# Yocto Search Engine

A tiny, yet functional, search engine.

## Introduction
This is an attempt to implement the basic components that comprise a small yet functional free text search scenario
on the Wikipedia XML english page dump. From corpus manipulation, indexing and persistence to basic retrieval from
a simple command line interface. It was intended in order to familiarize with the basic notions and data structures
that revolve around information retrieval. Even though it does not pretend to be a fully functional implementation
of such a system, the size of the corpus (approximatelly 45G) posed some interesting memory usage and I/O blocking
challenges that was fun to implement and worthed the effort :) Hope this can be helpful for anyone interested in
understanding more things about searching text corpuses.

## Latest Status (16/04/2013)
Takes Wikipedia database dump files, builds and indexes on author and main text body. With a command line interface
you are able to issue search queries of single words or word prefixes which are ultimatelly resolved into a
list of article titles that contain the given term. It was an implementation choice, at least for this first version,
to have no dependencies on third party libraries. 

## Quick-Start Guide

### On your mark
First clone the repository in a directory of your choice:
```sh
# git clone git@github.com:vmous/jYoctoSearchEngine.git
```

### Get set
Jump into and use `maven` to assembly it in an executable `.jar` file
```sh
# cd jYoctoSearchEngine
# mvn package
...
```

The `.jar` file is created in the `target` directory.

### Go!
To run the indexer issue
```sh
# java -Xms1g -Xmx2g -cp jYoctoSearchEngine-1.0.jar yocto.cli.Index <path_to_XML_dump>
```

This will start parsing and indexing the XML dump file you provided and put everything in a directory called `index` besides the `.jar` file.

Now, go buy yourself a coffee :-) And don't choose Espresso, it'll take at least a couple of hours :-)

When done you can run the searcher
```sh
# java -Xms1g -Xmx2g -cp jYoctoSearchEngine-1.0.jar yocto.cli.Search <index_directory>
```
, where `<index_directory>` is the directory created by the indexer on the previous step.

After it initializes you end up with a prompt like this
```sh
Yocto Search #
```  

Issue your term queries
```sh
Yocto Search # google
```

You can also search authors/contributors
```sh
Yocto Search # author:prosody
```

You can even make prefix queries on both terms and authors, i.e
```sh
Yocto Search # author:prosopon
...
Yocto Search # author:prosody
...
Yocto Search # author:proso*
... + ...
```

At any point you can type `q!` to exit the search CLI. Don't forget to drop a comment or even a github star on your way out :)

## Tech Report

Check [here](resources/TECHREPORT.md) for some insights on the YoctoSearchEngine current implementation and future work.

## Build Status

Travis CI: [![Build Status](https://travis-ci.org/vmous/jYoctoSearchEngine.png?branch=master)](https://travis-ci.org/vmous/jYoctoSearchEngine)

## Licence

Yocto Search Engine is distributed under the [Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0).
