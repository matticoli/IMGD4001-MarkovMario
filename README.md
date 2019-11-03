# MarkovModel File Format
```
3 2  // 3 chunks, height 2
0.5 0.5 0 // probability distribution for other chunks proceding this one (space-separated)
# // actual level text
#
0.5 0 0.5 // next chunk
$ // all chunks are unique
#
0 0 0


// ^empty chunk for EOL
```