To use the taems distribution:

Type "make" to compile the distribution.  It assumes you have the
appropriate Java utilities in your path.

To create a library, type "make zip", which will create a taems.zip
file in the lib directory.  The taems distribution is dependant on the
utilities.zip file located in that same directory.

To view a textual taems file, type "make foobar.taems".

Documentation can be found online, or generated from the Java source
by typing "make doc".

Example Taems files are included with this distribution in the
test directory, more can be found online from
http://mas.cs.umass.edu/research/taems/white.

---

Release Notes:

- 1.3.1

Fixed some miscellaneous parsing bugs
Added more examples
Compatibility changes for Java 1.5

- 1.3

Added copy/paste
Added double-click task collapsing
Fixed parsing of number-prefix labels

- 1.2.2

Fixed /dev/null error in windows

- 1.2.1

Now supports legacy method_is_non_local when parsing
Fixed printing in newer JVMs
Printing scales the structure to the specified paper size

- 1.2.1

Fixed printing bounds generation
Fixed two token attribute parsing

- 1.2

Added editing capabilities

- 1.1

Changed the library into an executable jar for simplification

- 1.0

Initial release
