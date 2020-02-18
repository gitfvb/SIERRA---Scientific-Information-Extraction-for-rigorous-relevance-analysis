# SIERRA - Scientific-Information-Extraction-for-rigorous-relevance-analysis

This is the source code of my bachelor thesis where I needed to use Latent Dirichlet Allocation (LDA) to automatically create tags for text documents. The process is as follows:

* Crawling: Define a website to search for links that end in a pdf file like scientific papers
* Extraction: Open the pdf files and convert them into a machine readable text format
* Formatting: Remove stop words and other unimportant meta information of those files
* LDA: Automatically use scala and java to create tags for the scientific papers. The topics are generated automatically at the machine learning process. A topic always consists of multiple keywords

This is very similar to the search engine fields where you type in a word and you get the next words. LDA is also one of the algorithms used by Google.

# Files

* This repository contains my bachelor thesis and the scientific paper that was written on the base of this software and the thesis
