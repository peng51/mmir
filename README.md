# mmir
Multimodal Information Retrieval

In this project, we designed a few fusion approaches to combine image retrieval and text retrieval to improve the retrieval qualtiy.

Under the src/mmir/ folder, are the programs related to implement the multimodal fusion to combine image-only retrieval and text-only retrieval.

Two datasets were prepared in the data/base/ folder, including a multimodal dataset downloaded from Google and another one downloaded from Twitter.
Correpsonding queries for both datasets are located in the data/query/ folder.

The project is written in Java and should be complied using Eclipse in Linux/Ubuntu environment. 

A Solr server is required to lanuch when running the project. Solr 4.6.1 is recommended. Newer or older versions of SOlr may cause some incompatibility issues.
