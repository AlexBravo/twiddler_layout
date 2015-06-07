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
Symbols can be modified with [LSHFT] (like for capitalizing letters), [LCTRL], [LALT], or [LGUI] (which stands for the Windows button, or the Command button on Mac).  The L prefix just means the left button on your full sized keyboard, there are R versions as well.  When you want to modify a symbol, you have to let the program know that it's a single symbol, so it has to be enclosed in an additional level of []'s.  So Control+C would be: [[LCTRL]c].
#### Escaped characters
Since [ and ] are used to demarcate non-single-character symbols, and we also sometimes need to specify the symbol '[' and the symbol ']', we will have to "escape" them, so when we want '[' we have to use '\\[', and when we want ']' we have to use '\\]'.  Note that this escaping only needs to be used in the chord mapping file, when typing on the twiddler, you don't have to worry about escapes.  Since we have to use \ for escaping, when we want '\', we have to use '\\\\'.
Single symbol examples:
* O ORMO    a
* S ORMO    A
* O OLOM    \\[
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
* Seems obvious that the twiddler will be slower, you lose the alternation between hands, and  also two handed chords such as [left-shift]-['] producing ["].  There are only 12 buttons so some letters will have to be chords.  We want to regain as much speed as possible.
* It would be great if all the letters were single button presses on the twiddler, like they are on the full keyboard, do we have to give up on this?  Take advantage of the fact that a string is sent when a single button of a chord is released, treat buttons held through transitions as Open for the transition.  (Not perfectly true, long stretches with close fingers are not as easy as Open buttons.)
