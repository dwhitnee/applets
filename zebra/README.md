This is my solution to a MacTech Programmers Challenge from 1997. It's in applet form so it wont run anymore, but here's the code. Perhaps I'll turn it into a Java App someday, or convert to Javascript.  It is written in Java 1.0.

See Zebra.html for more information.  The challenge email follows:


<code>

Date: Mon, 15 Sep 1997 06:48:20 -0400
To: CHALLENGE-A <CHALLENGE-A@listmail.xplain.com>
From: Bob Boonstra <boonstra@ultranet.com>
Subject: October 1997 Programmer's Challenge Problem Statement
Sender: <CHALLENGE-A@listmail.xplain.com>
Precedence: Bulk
X-MIME-Autoconverted: from quoted-printable to 8bit by Xenon.Stanford.EDU id DAA25677

Enclosed is the Programmer's Challenge for October 1997.

Mail solutions to:
  <mailto:progchallenge@mactech.com>, and copy
  <mailto:bob_boonstra@mactech.com>
Due Date:  11:59PM, EDT, 1 October 1997

Test code is available at:
<ftp://ftp.ultranet.com/pub0/b/boonstra/Challenge/ZebraTestCode.sit.hqx>

--Bob

-------------------
Who owns the Zebra?

I'm finishing up this column while at the beach on vacation. The place
we're staying is kind of interesting, in that all of the condominiums look
exactly alike, except that each has a different color door. Even more
interesting, each condominium is occupied by a person of a different
nationality, and each person owns a different kind of pet. There are three
condominiums, with red, green, and blue doors. They are occupied by an
American, a Canadian, and an Australian (but not necessarily in that
order). The American vacationer lives in the house with the red door. The
person in the house with the blue door owns a dog. The person who owns the
cat lives in the middle house. And the house with the green door is
immediately to the right of the house with the blue door. So, who owns the
zebra?

Well, I really am on vacation, but pets aren't allowed, the doors are all
the same color, and I don't know the nationalities of my neighbors. But I
did run across a zebra puzzle recently, and it seemed like a good logic
problem for the Challenge. In the above example, you can reason through the
four clues to rule out most of the 216 possible combinations and conclude
that the American owns the zebra. Problem complexity grows rapidly with the
number of variables - in a problem with 5 variables, there are more than 24
thousand million (that's 24 billion for Americans) combinations, but the
zebra can be found with as few as 14 clues.

Your Challenge this month is to write a program that will reason through a
set of clues and provide a solution consistent with all of the clues. The
problem will be provided in a stilted syntax - for example, the sample
problem above would be given as follows:

  American ISA person
  Canadian ISA person
  Australian ISA person
  redDoor ISA house
  greenDoor ISA house
  blueDoor ISA house
  dog ISA pet
  cat ISA pet
  zebra ISA pet
  person lives_in house
  person owns pet
  American lives_in redDoor
  blueDoor owns dog
  cat IS_LOCATED IN_MIDDLE
  greenDoor IMMED_RIGHT_OF blueDoor
  SOLVE person owns zebra
  ANSWER person house pet

The nine ISA relations define the variables (person, house, pet) and the
values those variables assume in the problem. The next two statements
define the relations (lives_in, owns) between selected pairs of variables.
The next four statements are the clues describing relations between values
of variables, discussed further below. The SOLVE statement defines the
question that you are to answer, and the ANSWER statement defines the
format that your solution should take.

The prototype of the code you should write is:

void WhoOwnsZebra(
  long problemDimension, /* number of problem variables */
  long numClues,         /* number of clues provided */
  CStr255 clues[],       /* the clues */
  CStr255 solution[]     /* storage for problemDimension result strings */
);

The problemDimension parameter describes the number of variables in the
problem you are to solve (in the example above, problemDimension was 3).
The number of clues provided is given as numClues (17 in the example). The
solution is to be provided as a sequence of problemDimension n-tuples that
form a solution to the problem, where each n-tuple is a sequence of values
in the order described by the ANSWER clue. In the example given above, one
solution would be:

  Australian  blueDoor   dog
  Canadian    greenDoor  cat
  American    redDoor    zebra

The clues will consist of a sequence of case-sensitive tokens separated by
spaces. The clues will take one of the following forms, where tokens in all
caps are reserved words:

  value ISA variable
  variable relation variable
  value relation value
  value IS_LOCATED [AT_LEFT | IN_MIDDLE | AT_RIGHT]
  value [NEXT_TO | IMMED_RIGHT_OF | IMMED_LEFT_OF] value
  SOLVE variable relation value
  ANSWER variable ... variable

The ISA reserved word is used to define the variables in the problem and
associate legal values with those variables. The relation statement takes
two forms, one that defines a relationship between two variables, and one
that associates a value taken by one variable with a value taken by
another. These associations are transitive (e.g., if the American lives_in
the redDoor house, and the person in the redDoor house owns the zebra, then
the American owns the zebra). The relations associate values, and the
specific words used to define a relation have no meaning except to make the
problem more readable. In addition to the relations defined by the problem,
there is a left-to-right ordering of the n-tuples in the solution. The
special predefined NEXT_TO, IMMED_RIGHT_OF, and IMMED_LEFT_OF relations
provide information about the relative left-to-right ordering of values.
The predefined IS_LOCATED relation associates values with three fixed
points in the left-to-right ordering: AT_LEFT | IN_MIDDLE (middle position,
meaningful only for odd values of problemDimension), and AT_RIGHT
(rightmost position).

There may be more than one set of n-tuples that solve the problem, so the
solution you report need not be unique, as long as it is consistent with
all the clues. Enough clues will be provided to uniquely answer the
question that you are asked to SOLVE, and you may use this fact in
directing your search. The n-tuples provided in the solution should be
provided in left to right order.

There are no memory restrictions on this problem, except that it must run
on my 96MB 8500/200. You should deallocate any dynamically allocated memory
before returning. This will be a native PowerPC Challenge, using the latest
CodeWarrior environment. Solutions may be coded in C, C++, or Pascal.

Now, back to the beach to find that zebra ….


-- Programmer's Challenge        -- Bob Boonstra
   progchallenge@mactech.com        boonstra@ultranet.com
   <http://www.mactech.com/>        <http://www.ultranet.com/~boonstra>


</code>
