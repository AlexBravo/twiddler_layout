# twiddler_layout
---
###Brief description of Twiddler (http://twiddler.tekgear.com/):
---
The Twiddler is a programmable one handed "chording" keyboard.
It has a row of three buttons (**L**eft, **M**iddle and **R**ight) for each finger, and also four modifier buttons for the thumb.  The default labels for the thumb buttons are **N**um, **A**lt, **C**trl and **S**hft.
Only one button at a time can be pressed per finger, but many fingers can press buttons all at once to form "chords".
When at least one finger releases it's button, the Twiddler sends a character (or characters) to the computer (as a keyboard).  You can specify what character (or string of characters) is sent by the Twiddler when a chord is used within a binary configuration file (http://goo.gl/Oeidh0).

One simple notation used to denote chords: [thumb] [space] [pointer] [middle] [ring] [pinky]

i.e. **CA OLMR**, meaning:
* thumb presses **C**trl and **A**lt
* pointer is **O**pen
* middle presses it's **L**eft button
* ring presses it's **M**iddle button
* pinky presses it's **R**ight button

Note: The meanings of Left, Middle and Right are as you are facing the buttons you are pressing.

---
### Description of the command line interface
---
There is a graphical layout modification tool (http://twiddler.tekgear.com/tuner/), but it is not very nice to use, and is geared more toward changing one or two mappings, rather than remapping everything.  This project is meant to be a command line tool (and also functionality which can be used by other more graphical things) to provide the following functionality:
* **--translate** back and forth between simple text and twiddler binary configuration formats
* **--xray** a layout, switching the **L**efts and **R**ights of things if you want to hold the twiddler with buttons facing away, and imagine looking through the twiddler to the keys
* **--swapHands** such that you can switch hands, taking advantage of what you've memorized
* **--thumbRemap** a layout such that everything stays the same except the thumb buttons.  The motivation for this is that many chords are implied, if you don't specify a capital A, you can still get one using the Shft thumb button and your lower case a.  If you want to move the Shft button to a different thumb button, you no longer get these implied chords, and have to specify each and every one.
* **--combine** layout (fragments), so you can say "I like how person A did the letters, but how person B did their number pad", and get both.  What it actually does is take the first layout, and overwrite it with anything present in the second layout.  You would have to have the number pad defined separately by itself in a layout (I think of this as a layout "fragment").
* **--fillImplied** chords in a layout... not sure why a person would want to do this, but it's an intermediate step for the thumbRemap, and I thought I'd like to see **everything** the twiddler would do, and not leave anything implied.

---
### Description of the simple text format used
---
One chord, described above, a tab, the string that chord should produce.  The strings for a chord can be a single symbol, or can be are made up of a string of symbols.
#### Symbols
The word "symbol" is used because some keypresses don't produce letters, like the delete key.  If you want to emit what the delete key emits, it's specified with the symbol [DEL].  Since such symbols are made up of multiple characters, they are enclosed in [].
#### Strings
If you want a chord to emit a string of symbols, just specify one symbol after the other, such as "The".
#### Modified Symbols
Symbols can be modified with [LSHFT] \(like for capitalizing letters), [LCTRL], [LALT], or [LGUI] \(which stands for the Windows button, or the Command button on Mac).  The **L** prefix just means the left button on your full sized keyboard, there are **R** versions as well.  When you want to modify a symbol, you have to let the program know that it's a single symbol, so it has to be enclosed in an additional level of []'s.  So Control+C would be: [[LCTRL]c].
#### Escaped characters
Since [ and ] are used to demarcate non-single-character symbols, and we also sometimes need to specify the symbol '[' and the symbol ']', we will have to "escape" them, so when we want '[' we have to use '\\[', and when we want ']' we have to use '\\]'.  Note that this escaping only needs to be used in the chord mapping file, when typing on the twiddler, you don't have to worry about escapes.  Since we have to use '\' for escaping, when we want '\', we have to use '\\\\'.
Single symbol examples:
* O ORMO    a
* S ORMO    A
* O OLOM    \\[
* O OMOL    \\\\
* O ROOO    [TAB]
* S ROOO    [[LSHFT][TAB]]
* CA OOLO   [[LCTRL][LALT][DEL]]

Multi symbol examples:
* O MLMO    The
* O LMLO    [[LCTRL]x][[LCTRL]o]y

---
### Goals (why I wrote this, what I want to use it for):
---
* Think Dvorak.
* Optimize the letter layout (prime use case) in terms of ergonomics of individual chords, ergonomics of chord transitions, probabilities of individual characters,  and probabilities of character transitions in streaming text.
* Keep the twiddler general purpose like a full keyboard.  This means that since we don't know all the contexts it will be used in, don't accidentally eliminate combinations available on the full keyboard.  (i.e. Don't use Ctrl-c to define the string "can't", because what if you start using a new program where you need Ctrl-c?)
* When there are trade-offs between contexts, prefer the context of typing english text.  This just means, that even though I'm a programmer, and type the word "class" all the time when writing programs, most people using a keyboard aren't.  Don't skew vocabularies, and don't add "class" as a string for an easy chord when that easy chord could be used for something more "typing english" based.
* Seems obvious that the twiddler will be slower.  On a full keyboard you have the alternation between hands, and  also two handed chords such as [left-shift]-['] producing ["].  There are only 12 buttonson the Twiddler so some letters will have to be chords (as opposed to single button presses).  We want to regain as much speed as possible.
* It would be great if all the letters were single button presses on the twiddler, like they are on the full keyboard, do we have to give up on this?  Take advantage of the fact that a string is sent when a single button of a chord is released, treat buttons held through transitions as Open for the transition.  (Not perfectly true, long stretches with close fingers are not as easy as Open buttons.)
* We shouldn't sacrifice long term speed for memorability.
* Anything that is rare (special characters) should be as memorable as possible.

#### Some numbers
Ignoring the thumb buttons, there are:
* 12 1-button combinations (4C1 * 3^1) = 4 * 3 = 12
* 54 2-button combinations (4C2 * 3^2) = 6 * 9 = 54
* 108 3-button combinations (4C3 * 3^3) = 4 * 27 = 108
* 81 4-button combinations (4C4 * 3^4) = 1 * 81 = 81
* 255 total combinations (4^4 - 1) = 256 - 1 = 255

Separating the three strongest fingers (pointer, middle and ring):
* 27 2-button strongest-3 combinations (3C2 * 3^2) = 3 * 9 = 27
* 27 2-button non-strongest-3 combinations (3 * 3C1 * 3) = 3 * 3 * 3 = 27 (= 54 - 27)

---
### My specific efforts
---
* What I was trying to do was maximize the number of "walking" transitions, while typing in english.  A "walking" transition is one where only one key is released, and one new key pressed, all other keys remain held.  The idea is that walking transitions are barely harder than sequences of single key presses.  Hopefully this will bring single key press ease to the much larger space of chords.
* My secondary objective was to assign the most often used letters in english to easier (more ergonomic) chords.
* You could build on the code here to do many different things (try to optimize on other criteria).
* I restricted my efforts to laying out the letters (and space) on two-key chords, using only the strongest three fingers.  (There are 27 such chords, a nice fit.)
* I used a set of word counts from google to generate two-letter ("bigram") frequencies, with the assumption that all words areseparated by spaces (ignoring punctuation, hopefully a small error).
   * There is a very thorough analysis of n-grams here: http://norvig.com/mayzner.html
   * The file linked to by that article with the google words is here:  http://norvig.com/google-books-common-words.txt
   * Through this analysis (with my assumptions), I found that 4% of transitions are from 't' to space, or from space to 't'.  Every 23 characters (on average) you're starting or ending a word with the letter 't'.
* I began with a hand made assignment of the 27 characters to the chords.
* I wrote a little steepest ascent local search to tweak the hand made layout for better bigram coverage.  The neighborhood I used was the swapping the assignment of ever pair of letters.
* Random layouts cover about 40% of transitions, pathalogical layouts (local search for worst), still cover about 20% of transitions, and the best I've found are between 65% and 66%.

---
### Some data that went into the optimization
---
* chords are in somewhat reasoned, somewhat imperically derived descending order by ergonomics, reasoned factors include:
   * nearness of fingers (no gap vs gap betwee fingers)
   * extremeness of button gap (adjacent fingers spread from L to R is more difficult)
   * define M to be easiest column (determined by rotation of device)
   * correspondence of chord to finger length (middle finger should use buttons further to the left of index finger)
   
letter | frequency | transition | frequency | chord
-------|-----------|------------|-----------|------
E | 0.1249 | t_ | 0.0430 | O MMOO
T | 0.0927 | e_ | 0.0396 | O RROO
A | 0.0804 | s_ | 0.0338 | O LLOO
O | 0.0764 | er | 0.0255 | O MLOO
I | 0.0756 | a_ | 0.0250 | O RMOO
N | 0.0723 | ht | 0.0241 | O LMOO
S | 0.0651 | d_ | 0.0227 | O MROO
R | 0.0627 | o_ | 0.0203 | O OMMO
H | 0.0505 | eh | 0.0203 | O ORRO
L | 0.0406 | n_ | 0.0200 | O OLLO
D | 0.0381 | in | 0.0181 | O OLMO
C | 0.0334 | it | 0.0161 | O OMRO
U | 0.0272 | an | 0.0152 | O ORMO
M | 0.0251 | f_ | 0.0150 | O OMLO
F | 0.0240 | r_ | 0.0150 | O MOMO
P | 0.0213 | es | 0.0148 | O LOLO
G | 0.0186 | no | 0.0145 | O RORO
W | 0.0167 | en | 0.0140 | O LOMO
Y | 0.0166 | i_ | 0.0138 | O MORO
B | 0.0148 | at | 0.0132 | O MOLO
V | 0.0105 | or | 0.0131 | O ROMO
K | 0.0054 | de | 0.0126 | O ROLO
X | 0.0023 | h_ | 0.0119 | O LORO
J | 0.0015 | y_ | 0.0116 | O RLOO
Q | 0.0012 | ar | 0.0115 | O LROO
Z | 0.0008 | is | 0.0109 | O OLRO
  |        | ... | ...   |

---
### The letter layout I'm going to start learning
___

chord | letter
------|-------
O MMOO | [SPC]
O RROO | x
O LLOO | b
O MLOO | e
O RMOO | i
O LMOO | s
O MROO | l
O OMMO | t
O ORRO | p
O OLLO | g
O OLMO | h
O OMRO | o
O ORMO | y
O OMLO | n
O MOMO | a
O LOLO | j
O RORO | f
O LOMO | w
O MORO | r
O MOLO | d
O ROMO | c
O ROLO | k
O LORO | u
O RLOO | v
O LROO | q
O OLRO | m
O ORLO | z
