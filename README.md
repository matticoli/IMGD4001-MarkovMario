## Build instructions

To build, open project in IntelliJ Idea and click the run button in GenerateLevel.java. See file structure section below and comments for alternative parameters

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


## Copyrights
This project is based off of [amidos2006's Mario AI Framework](https://github.com/amidos2006/Mario-AI-Framework) - see copyright disclaimer below

This framework is not endorsed by Nintendo and is only intended for research purposes. Mario is a Nintendo character which the authors don't own any rights to. Nintendo is also the sole owner of all the graphical assets in the game. Any use of this framework is expected to be on a non-commercial basis. This framework was created by Ahmed Khalifa, based on the original Mario AI Framework by Sergey Karakovskiy, Noor Shaker, and Julian Togelius, which in turn was based on Infinite Mario Bros by Markus Persson.
