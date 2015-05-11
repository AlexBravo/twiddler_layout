# twiddler_layout
code for easy creation and modification of twiddler chord maps
* a command line app that will translate between twiddler's binary format (http://goo.gl/Oeidh0) and a simple two column delimited file, listing chords and the keystrokes the chords should send.
   * will need to define short symbols for non-single-characters (i.e. TAB, DEL, ...) 

later perhaps:
* define difficulty of individual chords and difficulty of transitions between chords
   * finger strength issues: middle, index, ring, pinkey
   * finger length issues (which is harder between  LMRO and RMLO)
   * finger skipping issues (MOMO seems harder than MMOO or OMMO)
   * finger switching transitions seem harder -- maybe not, maybe only target matters? (MOMO->OMOM vs MOMO->MMOO vs MOMO-> MOLO)
* analyze a corpus of english text, books etc (my own gmail account?) to find unigram and bigram frequencies for english
   * unigrams: etaoin shrdlu? 
* define an objective function in terms of the above two
* see what kind of optimization can be done, come up with a better "dvorak" layout
   * used a GA a long time ago, lost the objective, how will that old layout score?  local search?
