## Project structure
`levels/levelBreakdowns/` Input files from manual analysis

`levels/autoAnalysis/` Sample output from analyzer

`levelGenerators/Markov/` Markov level generator package (Generates level from MarkovModel object)

`Markov/` package containing MarkovModel utility class (contains file IO, level analyzer, and Markov file format)

## MarkovModel File Format
```
3 2  // 3 chunks, height 2
0.5 0.5 0 // transition probabilities for all chunks in order (space-separated)
-------------
-------------
-------------
-------------
-------------
-------------
-------------
-------------
-------------
-------------
-------------
-------------
%%%%%%%%%%%%%
|||||||||||||
|||||||||||||
|||||||||||||
0.5 0 0.5 // next chunk
...
```