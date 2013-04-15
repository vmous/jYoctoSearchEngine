# Yocto Search Engine

A tiny, yet functional, search engine.

## Quick-start guide

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
Yocto Search # soundcloud
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

## Build Status

Travis CI: [![Build Status](https://travis-ci.org/vmous/jYoctoSearchEngine.png?branch=master)](https://travis-ci.org/vmous/jYoctoSearchEngine)

## Licence

Yocto Search Engine is distributed under the [Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0).
