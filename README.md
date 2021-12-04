# hmm

## CS10 PS5

COSC10: The Hidden Markov Model

## Viterbi Testing Report

To test the viterbi() method, I modified POSLib.java’s loadData(). I made it create maps for observations and transitions and had it return a list of maps where the first map is the observations map and the second is the transitions map. I used the example-hmm file (converted from excel to a txt file) on loadData() to generate the HMM. Then I used file-based testing code to compare the tags resulting from the viterbi() method’s assessment of the example-sentences.txt file to the tags in the example-tags.txt file. This gave me 43 correct tags out of a total of 44 tags, which is a ~98% accuracy for a file of this size.  

## Short Report (tagged as expected vs unexpected tags)

For this, I tested using the simple HMM and console-based testing. 
Sentences used: 

1.	“this is my night .”

[pro, v, pro, n, .]
Although “this” was never used as a demonstrative pronoun in the train sentences, it is tagged correctly (kind of) as pronoun by the test.

2.	“my dog is your dog .”

[pro, n, v, pro, n, .]
This is an example of a correctly tagged sentence.

3.	“my dog trains in the night in a cave .

[pro, n, v, p, det, n, p, det, n, .]
Another correctly tagged sentence.

4.	“my dog wants to live in a hotel”

[pro, n, v, det, n, p, det, n]
Some incorrect tags here: “to” is labeled det instead of preposition, and “live” as noun instead of verd. This is because I used words not in the HMM like “to.”

## File-Based Testing Performance

### Results of example-hmm:

* # of tags correct: 43 
* # of tags wrong: 1 

### Results of simple: 

* # of tags correct: 32 
* # of tags wrong: 5 

### Results of brown:

* # of tags correct: 35109 
* # of tags wrong: 1285 

## Unseen Penalty (-10.0 vs -100.0) 

For example-hmm and simple test cases, using either -10 or -100 for the unseen penalty yielded the same results. However, for brown, using -10 gave me 4008 incorrect tags, whereas using a harsher penalty score like -100 significantly improved results by giving me 1285 incorrect tags. This is probably because the files are shorter and have a smaller selection of words for training. As such, the test sentences (which mostly have words already in the HMM) are less likely to be incorrectly tagged, so unseen penalties don’t make much of a difference. 
In a larger file, there are probably more opportunities for some words to only appear once or twice among thousands of words which increases the probabilities for each tag, making it possible for frequent incorrect tagging. So larger files are impacted noticeably by unseen penalties as it counters some of these heightened probabilities. 
