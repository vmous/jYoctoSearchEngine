# YoctoSearchEngine's Yocto Tech Report.

## Indexing

The YoctoSearchEngine uses the widely acclaimed and used *inverted index* as its key data structure to build its full-text indexing capabilities. An inverted index is simply a mapping from data to data containers. In our case, the mapping is from *terms* (*words* or *tokens* are some time alternative parlances) to *documents*.

### Parsing

This step involves the parsing of the corpus to identify documents. If the corpus is a set of HTML files then the documents are the actual .html files. In our case, the test corpus was the [latest english Wikipedia dump][1]. The implementation handles [BZip2 compressed][2] (GZip as well) dumps but it is highly recommended you inflatete it inorder to save time during multiple indexing processes.

### Analysis

This part is one of the most critical ones, directly effecting a search infrastructure's recall and precision factors but also resource usage (I/O, CPU usage e.t.c.) during both indexing and searching. The processing is done on a lexicographic level so that the important terms from each document are identified. The output of this step is a *forward index* mapping a list of such terms per document. Following are some substeps that accumulativelly refine the outcome:

1. [*Language detection*][3]
   Identifies the language of the document. The next steps need to be aware of the language of the document they are handling since lexical analysis is obviously tightly coupled to the language itself. Moreover, the following steps per se, assume an alphabetic language. In other cases, like the ideographic Japanese language, the following techniques are not applicaple and require different, specialized technologies.
 
2. *Tokenization*
   This process converts the input document from a character stream to a list of tokens. A token is a single alphanumeric entity/word. Subtasks involve: 
   1. Lower-casing the entire character stream.
   2. Cleanning up text from any markup noise (i.e., HTML, wiki markdown e.t.c.). This takes a significant proportion of the preprocessing stage process. Regular expressions used to handle should be chosen carefully and
   3. Sequences of characters are splitted on non-alphanumeric characters (whitespace, punctuation) and are put into a token list.
 
3. *Stopword removal*
   The output of the tokenization is compared against a standard set of non-significant words, specific for the language of the document, called stop-words. These tokens are ignored and not put into the document's tokens list. There can be various levels of stopword removal, from basic to aggressive. The first retains possibly important information within the index with the expense though of extensive resource usage (e.g., disk space). The second is the exact complementary of the first. As always a balance must be found. Our implementation uses the [MySQL full-text stopword list for english][4]
 
4. *Stemming/Lemmatization*
   Both techniques are used to reduce inflectional forms and sometimes derivationally related forms of a word to a common root form. Stemming is a brutal heuristic that cuts the ends of words off. Lemmatization does NLP (vocabulary and morphological analyses of words) in an attempt to return the root dictionary form of the word. This can heavily reduce the size of the index, speed up query execution and broaden the recall scope of the engine. Implementation of such processing was out of the scope at this point and dependencies should be eliminated thus no such functionality is currently offered.

### Inversion

Inversion is the process of creating the inverted representation of the corpus from the forward indices we have after the parsing and analysis phases. Assuming we have a stream of of nicelly normalized tokens, we first create a sorted list of these tokens, and merge them togeather to get a term to list of document ids the term apears into, i.e.

```
<TERM> <DOCID>

the 1
smiths 1
were 1
an 1			<TERM> -> <POSTINGS_LIST>
english 1
alternative 1		1982 -> 1
rock 1			1990 -> 2
band 1			alternative -> 1
formed 1		american -> 2
in 1			an -> 1,2
manchester 1		band -> 1,2
in 1			english -> 1
1982 1			formed -> 1,2
			in -> 1,2
		=>	is -> 2
			jam -> 2
pearl 2			manchester -> 1
jam 2			pearl -> 2
is 2			rock -> 1,2
an 2			seatle -> 2
american 2		that -> 2
rock 2			smiths -> 1
band 2			the -> 1
that 2			washington -> 2
formed 2		were -> 1
in 2
seattle 2
washington 2
in 2
1990 2
```

This is called sort-based indexing. Advantages become apparent both during the indexing and retrieval processes. In the indexing when we need to perform in-memory or external merging operations, while in retrieval for faster range-like dictionary searches e.t.c. In real-world search engines, additional statistical information is kept in the inverted index, like term frequency, inverted document frequency, term positioning e.t.c. These stats can back more sophisticated query support (e.g., proximity, phrase) or result ranking (cosine similarity on a TFxIDF-based vector space).

The resulting structure is made of two basic components, a term dictionary on the left and the postings lists on the right. As we will later see, the former will be loaded in-memory serving as a look-up that points (using offsets) to postings lists, the later, which are much larger in terms of volume and are be kept on disk files.

The fastest way to construct this data structure would be to do everything in-memory but for large corpuses this is not possible. We, thus, handle the the inversion in a batched manner. We implemented a version of the **Single-Pass In-Memory Indexing (SPIMI)**[5] algorithm that is proven to exhibit excelent scaling properties as long as there is disk space available. The algorithm takes as input a batch of documents that have been extracted from the corpus. For each such document it processes its token stream, term by term and constructs the inverted structure. At the end of each batch processing the memory resident inverted index is persisted to disk and the SPIMI inversion is then called for the next document block. Finally, all blocks on disk, are ultimatelly merged into one. More details on persistence and merging, check the storage section of this report.


### Stored fields

With the current infrustructure, a term can be resolved into a document id. This is a piece of information that would be hard to use if the application on top of it is targeting human users. Thus, we needed a way to store information that could be retrieved and offered to the user as are result of a query. First thought would be to store this info inside a posting object (along with the document id) and stored into the main index file on disk. Unfortunatelly, this would result into huge amount of duplicated info since a posting for a docid may apear thousands or even milions of times inside the index. We would need this information be stored in a different file and an dictionary that will enable the additional level of redirection from document id to document stored fields. Continuing our band example above the structure for stored fields look like

```
<DOCID> -> <STORED_FIELDS>

1 -> The Smiths
2 -> Pearl Jam
```

Currently our search engine supports only a human readable "label" (the wikipedia article title) but the data structure is open to support multiple fields from other strings to BLOBs.


## Storage

### Index and segments

As we saw during our quick discussion on the SPIMI algorithm, each call of the inversion process writes a block to disk. During indexing this block represents a fraction of the final index, thus we call it a *segment*. The index and segment have the same represantation on disk. They both comprise a *postings offsets file* (indx.off/_seg.off.X) and a *postings file* (indx/_seg.X). The format is the following
```
POSTINGS OFFSETS FILE
<TERM(string)><OFFSET(long)>

POSTINGS FILE
<POSTINGS_LIST_SIZE(int)><POSTING_1(long)>...<POSTING_N(long)>
```

The offsets file is loaded in memory, in a shorted manner using a tree based data structure. This enables fast search and other features and benefits (e.g. prefix queries). The offset value that is mapped with each term, is the offset within the postings file that the postings list associated with this term can be found. The postings file remains on disk and is accessed randomnly using the offsets. Postings lists are stored on disk as a contiguous run of postings without explicit pointers, so as to minimize the size of the postings list and the number of disk seeks to read a postings list into memory.

### Store

Similarly to the index and segment files, the store also consists of a *store offsets file* (store.off) and a *store file* (store). Its format looks like
```
STORE OFFSETS FILE
<DOCID(long)><OFFSET(long)>

STORE FILE
<FIELD(string)
```

Again, at run time, the offsets file is loaded in-memory to play the role of the look-up table for the store file. The redirection we seek here is more naive since we just want to map from a doc id to its corresponding stored fields as quick as possible. Thus we choose a hash based data structed.

### Merging

The last step of SPIMI is to merge the blocks created during the inversion calls into the final inverted index. Typically this makes the whole indexing proccess, heavily I/O bound. The first step on refining the process is triggered by the observation that each inversion of a document batch takes a significant amount of time and the whole process (after parsing which takes at least an order of magnitude less time that the rest of the processing) does not require I/O since it is done in-memory. We could thus assign a dedicated worker thread to merge on the background. For the future some testing with more merging threads (one or two) might reveal some extra room of improvement on this part. 

Another observation that lead to the refinement of the process was that prioritizing merges between smaller-sized segments, reduces the cumulative writes. 4 blocks of 100 bytes each yield 900bytes written if we do it (100,100)->(200,100)->(300,100)->(400) and 800byts if we do it (100,100)->(100,100)->(200,200)->(400). To implement this, we force each seagment created (either from the inversion process or a merge step) to be inserted to a priority queue which favours smaller sized segments. The merge scheduler when creating a merge job polls the smaller ones.

Another place that would yield significant improvement on merging is the observation that less postings movement reduces writes. These movements would be reduced if the merging job could be done on multiple segments at once. Currently the task operates on only two segments. Multiple segment merge was not implemented on this version due to lack of time :(

### Compression

Another important advantage of SPIMI over other inverting algorithms is that it can easily employ compression for both postings and dictionary terms. Compression increases the efficiency of the algorithm further because we can process even larger blocks, and because the individual blocks require less space on disk. For this first version of our engine we left compression out, but this look nice next step for improvement (and researching on the literature [6] :-). 

## Searching

YoctoSearchEngine implements the absolute basics in query evaluation. It supports a minimal range of query types. It can respond to single term queries and prefix queries. The later is done by appending the "*" characther to a prefix term you want to search for. It also supports searching on document author name by prepending the "author:" string exactly before the author name.

### Result Ranking

As discussed before, modern search engines are able to rank the results of a search query according to relevancy. Relevancy is often quantified through some short of similarity measurement (i.e., cosine in the vector space model). Another area for future improvement.


## Dependencies

This first version is intentionally stripped off all of possible dependencies on third-party software. Only dependency is JRE 1.7. Developed with OpenJDK 1.7.

## References

[1]: http://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2 (2013-Apr-04 22:47:57 9.0G application/x-bzip)
[2]: The only dependency in the project just in case that the test environent doesn't have the bunzip2 utility :-)
[3]: The corpus defined for this first attempt was the english Wikipedia pages so no language detection functionality was needed.
[4]: http://dev.mysql.com/doc/refman/5.5/en/fulltext-stopwords.html
[5]: http://nlp.stanford.edu/IR-book/html/htmledition/single-pass-in-memory-indexing-1.html
[6]: http://nlp.stanford.edu/IR-book/html/htmledition/references-and-further-reading-5.html

## Revisions

Last Update: 15/04/2013 (Vassilis S. Moustakas (vsmoustakas[at]gmail[dot]com))