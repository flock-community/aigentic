---
name: blog
description: Write, revise, shorten or review a post for the Aigentic blog (site/blog), such as a customer case, announcement or how-to. Use this whenever someone asks for a blog post or article for the site, or asks to edit, tighten or check one, in any language, even if they do not name this skill. Not for documentation pages under site/docs.
---

# Aigentic blog posts

A post has one message and one thread that runs from the first paragraph to the last. The house style gives the
result away in the title, the opening and the headings, and then tells the story in the sections, the way an engineer
tells a colleague. It uses British English. Posts are always in English, even when the request comes in Dutch. If the
author asks for something that departs from this style, do it and say which rule it departs from.

For `site/blog` this skill replaces the tone-of-voice guidance for the documentation. Older posts in `site/blog` were
not written in this style and are not examples of it. Do not take style, structure or wording from them.

The examples in this skill only show a shape. Never take a fact or a phrase for a post from them.

## The reader

The reader is a technical manager in Europe: an engineering manager, architect or CTO who decides whether to start a
project like the one in the post. Four facts about this reader explain the rules below.

- They are managers first. They follow a technical argument, but they come for the business problem, the outcome and
  the choices, not for the implementation.
- They skim first. They take in the title, the first paragraph and the headings, and then decide whether to read on.
- They have read many vendor blogs and trust the ones that state limits and mistakes.
- English is often their second language, so idioms, slang and long sentences cost them effort.

They keep reading for an outcome with a number, for the cost per unit of work, for the choices and the reasons behind
them, for what failed, and for something they can apply themselves.

## Kinds of post

Most posts are customer cases, and the rules are written with those in mind. The same style fits the other kinds, as
long as you take "the result" to mean the right thing:

- Customer case: what changed for the customer, measured.
- Announcement: what the reader can do now that they could not do before, and what is not there yet.
- How-to: what the reader will have at the end, and what they need to start.
- Opinion: the claim, in one sentence.

Use only what the material contains. If there was no failure, no cost figure or no measurement, do not add one to
satisfy a rule.

## Message, goal and thread

Settle these three before you write or change a sentence. A post without them can be correct in every sentence and
still be hard to read, because the reader cannot tell where it is going.

- The message is the one sentence the reader should remember a week later. In a customer case, say it in the
  customer's terms: what the customer can do now and what made that possible, not how the system works. If you need
  "and" to join two different ideas, you have two posts. Test it with a planning sentence that never appears in the
  post: "A reader who now thinks or does X should think or do Y, because Z." If you cannot fill in X, you have a
  topic and not a message.
- The goal is what the reader should believe or do after reading, and what that does for Aigentic. Examples: trust
  that a small model is good enough when you measure it; try Aigentic Platform on their own extraction task.
- The thread is the chain of three to six steps that leads from the starting situation to the result. Each section is
  one step, and each step follows from the one before it.

A customer case is the customer's story, and that story is the main line: it starts with the customer's problem in
the customer's terms, and it ends with what changed for the customer. The steps in between are how we got there, told
so that a manager can follow them. Build the case from what the material has:

- who the customer is (sector, size) and how they did the work before. Describe the old way with respect: it worked,
  and the reader probably works that way now;
- why it was no longer enough, and what was at stake;
- before and after as pairs on the same measure;
- the customer's own words, only as supplied. A quote earns its place when only this customer could have said it.

The title or the opening names the customer, or the kind of company when the name may not be used. A customer case is
not ready to publish until the customer has approved the text and every quote. Say in the handover note whether that
has happened.

The product appears as the thing that made a step possible, not as a separate subject. Test it by taking "Aigentic"
out of every sentence: a complete story of a problem and its solution should remain.

## How technical

Aigentic is a technical product, so a post may be lightly technical. The technique is never the main line. It appears
where it explains a result, a cost or a choice, and then the post returns to the customer. This holds for a revision
as much as for a new post: when the author's text is an engineering account, retell it for a manager.

These limits rank above keeping detail. Technical detail beyond what a manager needs is not part of the message, even
when it is true and interesting. Move it to the handover note as material for a technical post.

- Tell a technical step by what it did for the outcome. "Our library never let the model answer 'nothing', so it made
  something up" carries the point. The schema detail behind it does not have to be in the post.
- Tell a series of experiments as what we learned, not as a log. Three experiments are usually three sentences or a
  short paragraph each: what was wrong, what we changed, what the score did.
- In a customer case, the part about how we built and tuned it takes at most about half of the paragraphs. If it
  takes more, shorten those paragraphs before anything else. Do not pad the customer's side to mend the balance.
- Count the technical terms the reader has to learn to follow the post. More than about five is too many. Describe
  the thing in plain words, or leave it out.
- A customer case usually has no code. Take a code block out unless the reader has to see it to believe a claim, and
  never change code that stays. A how-to is the exception and has the code the reader needs.
- Leave out implementation detail that an engineer would enjoy and a manager would skip, such as parameter names,
  token counts per call and library internals. Name what you left out in the handover note, so the author can link to
  it or use it in a technical post.
- Test: someone at the customer who is not an engineer can read the post and retell it. Read only the first sentence
  of every paragraph. If those sentences are about models, prompts and fields more often than about the customer and
  the result, the post is too technical.

Then hold everything against the message. A fact, an anecdote or a feature that serves the message stays, and you tie
it to the step it belongs to. One that does not serve it goes out, however good it is, and you name it in the handover
note so the author can use it elsewhere. A long list at the end of a post is usually a second message.

When you revise, find the message the author was after. Read the title, the description, the opening and the last
section, and notice what the text keeps returning to. If the text carries several messages, choose the one that the
title and the ending point to, state your choice at the top of the handover note, and say what you left out because of
it. If you cannot tell which message the author meant, ask.

## Collect the facts first

Get the facts before you write a sentence. For a customer case you need:

- the result as counts, not only as a percentage ("27 of 28 invoices", not just "96%")
- what it was measured on, and what that leaves unproven
- the baseline: how the work was done before, and what that cost
- the cost now, per unit, in money or time
- each real choice, with the option that was rejected
- what went wrong and what caused it
- the exact model names and versions
- whether the client may be named; if nobody has said so, describe them ("a wholesaler")

Take facts from the author, from the repository and, when the Aigentic Platform tools are connected, from the
evaluation reports and cost overviews there. Check with the author that an evaluation on the platform is the one the
post describes before you use its figures. If the platform and the author disagree, ask the author. If you cannot
ask, keep the author's figure and name the difference in the handover note. Names on the platform can contain the
client's name, so do not copy them.

Do not quote values from client documents, such as names, order numbers or suppliers, unless the author says they may
be published.

Never invent a number, a name, a quote or a detail to fill a gap. If a fact is missing, or two numbers or two
statements disagree, ask the author. If you cannot ask, or the author does not have it:

- a fact nobody gave: leave the claim out;
- a fact the author gave imprecisely, such as a percentage without counts or "a few cents": use it exactly as given
  and do not make it more precise;
- two figures or statements from the author that disagree: in a new post use neither. In a revision keep the disputed
  words exactly as the author wrote them, and do not pick a side or recompute. You may still rewrite the sentence
  around them. Keep them out of the title, the opening and the headings where you can. If one of the two statements
  is itself a heading, write a heading that makes neither claim;
- a slip that the material itself settles, such as a tool name that the code or a screenshot in the post spells
  differently: correct it and report it;
- a claim nobody could back up, such as "never makes mistakes": this is a marketing claim and not a disputed fact, so
  cut it and report it;
- a fact that may have changed since the post was published, such as a date, a version or a regulation: do not update
  it yourself, but check it if you can and report what you found;
- in every case, name it in the handover note as something to settle before publication.

Screenshots and code in a post are part of the material. Look at them, and check the text against them.

Do not leave placeholders in the text. A wrong number that reads well does more damage than a missing one.

## Structure: the result on the outside, the story inside

A skimming reader sees only the title, the opening and the headings, so those carry the message and the result. A
reader who continues has chosen to read, and gets the story of how it went.

### Title

Say what happened, in at most about 12 words and without a colon and subtitle. If there is a number, include it
together with what it measures. Use sentence case, in the title and in every heading. Good: "Matching 40,000 invoices
a month with one agent". Weak: "Leveraging AI for finance: a deep dive".

### Opening

The paragraphs before `<!-- truncate -->` give the message, with the result as its evidence: what was achieved, who it
was for and what it replaced or made possible. Keep them under 80 words together. Most visitors read only this, so do
not spend it on a scene or on background. A percentage here says what it measures ("98% of the invoice lines"); the
counts follow in the body. If words are left, name the one or two concrete obstacles the post explains. They give the
reader a reason to continue.

### Headings and sections

Each heading states what its step of the thread showed, in at most eight words. Read in order, the headings form one
chain in which each follows from the one before, not a list of separate facts. "First we checked 48 invoices by hand"
followed by "Then every change to the prompt got a score" does that. "Experiment three" does not. One background
section with a descriptive heading is fine when the reader needs domain knowledge first. In a how-to, a heading states
what the reader has after the step. In an announcement, it states what they can do now.

The number from the title returns in one of the headings, so that a reader who skims the headings meets the result
again where the post reaches it.

Keep the number of headings down. A post of 1,200 words has five to eight sections, and a section has at least two
paragraphs. A finding that takes one paragraph to tell goes under the heading of the finding it leads to or follows
from, and that heading is worded to cover both. Three experiments that build on each other are one or two sections,
not three.

Inside a section, tell it in the order it happened: what we tried or expected, what we saw, what the cause turned out
to be, and where it ended, with the number. The heading has already given the ending away, so the section does not
have to open with it, and it needs no tricks to build suspense either. The text under a heading has to read correctly
without the heading, so it does not start with "That" or "This" pointing back at it. Add a limit where there is one.
Sections should not all have the same shape.

Let a section end where the next one starts: on the number that was not good enough yet, or on the problem the result
exposed. The next heading then answers it. Do not write a sentence whose only job is to announce the next section.

Put the sections in the order of events when each step explains the next. Otherwise put the most important finding
first.

A table is welcome when it compares three or more things on the same points, such as experiments with their change and
their score. A figure is evidence for one claim, and its caption states the finding ("Accuracy rose from 85% to 98%
over four prompt versions"), not the kind of figure ("Accuracy chart"). One diagram of the flow helps when describing
it takes more than a paragraph. Do not make a figure from numbers the author did not give; name the figure that would
help in the handover note. Check screenshots for client names and personal data.

### Ending

The last section brings the thread home. It says what the message means for the reader's own project, and in a
customer case what changed for the customer. Stop on the last useful fact. One plain closing line with a link to the
documentation or the code is allowed. Do not summarise the post, and do not close on a slogan or a pitch.

To check the structure, run the skim test: read only the title, the opening and the headings. They should give the
message and the thread. If they do not, fix them before you polish any sentence. Then imagine the headings in another
order. If nothing breaks, they are a list and not a thread.

## Voice: an engineer telling a colleague

Imagine telling a colleague from another company how it went, at a whiteboard. That is the tone: informal, precise and
without a sales pitch. You tell it as it happened, and you do not need to dress it up, because what happened is
interesting enough.

What makes it sound like a person:

- Use "we" and use contractions where a speaker would (didn't, we'd, it's).
- Say "you" when you give the reader something to apply.
- Say what we expected and what happened instead, where the material has it. "We expected the scans to cause most of
  the errors. The typed invoices caused more, because their tables have no lines." Not every section needs it.
- Start a sentence with "But" or "So" when a speaker would.
- Keep the concrete detail that belongs to this project only, such as the footer a renderer cut off or the column a
  supplier left out. Readers remember those.
- When something went wrong, say so plainly and give the cause. Say whose mistake it was when it was ours.
- A dry remark is welcome when it is short and true.

What keeps it readable for this reader:

- Give the number and let the reader judge it. Leave out superlatives, exclamation marks and words that tell the
  reader how to feel ("surprisingly", "interestingly", "the best part").
- Use plain words and no idioms. Write "this didn't work", not "a dead end"; "without configuration", not "out of
  the box".
- Keep sentences under about 25 words. A sentence may hold a claim and its reason. Write full sentences, and do not
  line up short dramatic ones.
- Give each thing one name. A proper name ("Gemini Flash"), "it" and one generic noun ("the model") are enough. Do
  not rotate synonyms, because a reader takes a new name for a new thing. Before you merge two names, check that they
  mean the same thing. An agent and its model are different, and so are a prompt and a task. A class and its member
  are different too: "a light model" is the class, "Gemini Flash" is the one we used.
- Explain a term a manager may not know in a few words the first time, and spell out an acronym once.

Read a paragraph back as if you said it aloud. If it sounds like an audit report, loosen it: add "we", a contraction,
what we expected or the concrete detail. If it sounds like a keynote, cut it.

## Evidence

- Give a percentage its counts the first time it appears in the body: "41 of 48, or 85%". Check the arithmetic.
- Say once per data set what it contains, how large it is and what was not tested.
- Where a real choice was made, name the options, the one we took and what it cost. "We kept the rules in the prompt
  and didn't fine-tune, because the rules change every month. The prompt is now two pages long."
- Give money in the currency of the source. Convert to euros only with a figure from the author.
- Use code as evidence, where a post has code at all (see "How technical"). Copy it from the project or the
  repository. You may leave lines out, but do not change the rest. Check Aigentic API names against the repository. A
  how-to needs what the reader has to type: write the smallest example that works and compile or run it.
- Mention a feature of Aigentic or Aigentic Platform at the point where the project needed it. Do not end with a
  product pitch. If the author wants to say what is being built next, give it one short section: prose for up to
  four features, a plain list of one or two sentences each for more. Tie a feature to the problem in this post that it
  solves, and separate what is available today from what is not. Do both only as far as the author's facts allow; do
  not guess which features exist.

## Length

A customer case usually needs 800 to 1,200 words of prose, and 1,500 at most. Front matter, headings and code do not
count. An announcement rarely needs more than 400 words, and a how-to is as long as its steps. Shorter is fine when
the facts fit; never pad. Compact means fewer points, not denser sentences. Keep the articles, the verbs and the
"because": a second-language reader loses more on a compressed sentence than on a longer plain one. When you shorten
a post, remove repetition, scene-setting and commentary first. Cut facts,
numbers and causes only when nothing else is left to cut.

## British English

- British spelling: optimise, analyse, normalise, behaviour, colour, centre, catalogue. Write "program" for software.
- Keep the original spelling of product names, UI labels, code identifiers and quotations. A feature called
  "Suggested normalizers" keeps that name.
- Dates as "14 November 2026". Metric units. Numbers as 1,200 and 99.9%.
- When you need an example, prefer a European one. Do not add regulations or context the author did not mention.

## Patterns to cut

These patterns make a text read as machine-written or as marketing. Leave them out of what you write, and take them
out of what you revise: a revised post meets the same standard as a new one. In a review, report them. When you fix a
pattern, rebuild the sentence around the fact. Do not swap it for a near relative, such as an em dash for a semicolon.

| Pattern | Example | Instead |
|---|---|---|
| Contrast with something nobody claimed | "This isn't just a library. It's a way of working." | Say what it does. A contrast is fine when we or the reader really believed the first half. |
| Dramatic beat | "It worked. Or so we thought." | One sentence with "but". |
| Teaser before a colon | "The result: 98%." | "It scored 98%." A colon after a full clause, before an explanation or a list, is fine. |
| Announcing | "Then something unexpected happened." | Start with what happened. |
| Question the writer answers | "So why does this matter? Because…" | Give the answer. |
| Labelling | "and this was the most valuable lesson" | Show the lesson. |
| Corporate register | "By manually validating the data, we were able to identify errors that were introduced during processing." | "We checked the data by hand and found the errors." Use an active verb and name who did it. |
| Systems with feelings | "the model was reluctant", "a score we could trust at last" | Plain verbs for what the model did (read, returned, ignored) are fine. Cut preferences, emotions and virtues. |
| Slogan pair | "Good prompts are written. Great prompts are measured." | One plain sentence. |
| Bold-label bullets | "**Speed.** It is fast." eight times | Prose, or a plain list. |
| Slogan ending | "The future of compliance is already here." | End on the last fact. |
| Marketing words | leverage, streamline, revolutionise, empower, robust, powerful, seamless | A plain verb, or the number. |
| Dash as punctuation | "It is fast — and cheap." | A comma, brackets or two sentences. |

Use bold for at most two or three domain terms the post depends on, once each. Use a list only for items that are
parallel and separate; two bullets are a sentence.

Removing patterns is not enough. A text also reads as machine-written when everything resolves: every question
closes, we are never stuck, and every section has the same weight. Where the material has it, keep what stayed open
and what was luck. Invent none of it.

More patterns, by category, are in [references/slop-patterns.md](references/slop-patterns.md). Read it before you
check a draft, revise a post or review one.

## The post file

- Location: `site/blog/YYYY-MM-DD-slug/index.md`. Wrap prose at 120 characters; front matter values stay on one line.
- Front matter: `title`; `slug` as `/kebab-case`; `description`, one sentence with the result; `authors`, keys from
  `site/blog/authors.yml`, and ask who the author is if you do not know; `tags`, reusing existing ones where they fit;
  `image` as `/img/blog/name.png` for a file in `site/static/img/blog/`. If there is no image, ask for one.
- Repeat the title as an `#` heading, identical to the front matter, then the opening, then `<!-- truncate -->`.
- Give every image alt text that says what it shows. Look at the image first; if you cannot, keep the existing alt
  text and say so.
- Do not change the `slug` or the dated folder of a post that is on the main branch, because links depend on them. If
  such a post has no `slug`, do not add one, because that changes the URL as well.
- Run `npm run build` in `site/` when the dependencies are installed, to catch broken links and front matter. If you
  could not run it, say so in the handover note.

## Workflow

### Writing a new post

1. Write down the message, the goal and the thread, and check them with the author.
2. Collect the facts and ask the author for what is missing.
3. Write the title and the headings first, and run the skim test on them.
4. Write the sections.
5. Check the draft: the message and the thread, the skim test, the arithmetic, the names, the spelling, the length and
   the patterns. Then read it once as a manager who has only this page. After each section, name what you now know
   that the heading did not tell you. If that is nothing, merge the section or cut it.
6. Hand over with a note in your reply, not in a file. Start with the message in one sentence and whether the post is
   ready to publish or what still
   blocks it. Then list the claims you could not verify, what you left out and why, and the decisions the author
   still has to make.

### Revising an existing post

Edit the file in place. The title and the description may change; the URL may not. Do not change the author's code:
keep a block as it is or take it out (see "How technical"), and report what looks wrong in it.

Keep what the author wanted to say, not the author's sentences. Find the message first (see "Message, goal and
thread"). Everything in the original that serves it stays: the facts, the points the author makes about the project
and the lessons for the reader. You add no claims. The wording is yours: rewrite freely in the voice of this skill,
and do not leave a stiff, passive or corporate sentence standing because the author wrote it.

Make the post compact by taking out what does not serve the message, and by cutting repetition, scene-setting and
commentary from the rest. Do not compress the sentences that remain. A post over 1,500 words has to come down. When
the rules cannot all hold, the message and its thread come first, then the limits on technical detail, then the
voice, then the length.

Before you hand over, go through the original paragraph by paragraph. Each fact, point and lesson is either in your
text or in the list of what you removed, with the reason. A lesson that serves the message stays even when it reads
like a closing line.

If the rules ask for a fact the post does not have, such as the counts behind a percentage, ask for it or name it in
the handover note. Report what you removed and every number that did not add up.

### Reviewing without editing

When asked only to review, change nothing. Start with whether the post is ready to publish. Then list the findings
with line numbers, most serious first: numbers that disagree with each other, percentages that do not match their
counts and claims without a scope; then the message and the thread (can you state them, and does every section
serve them?); then structure (the skim test); then voice, patterns and spelling. Group a repeated
pattern into one finding. Give a suggested rewrite for each.

## Examples

A section of a customer case, before:

> ## Our third attempt
>
> The third attempt was where things got interesting. We asked the model to copy the VAT number from each invoice.
> Surprisingly, it only managed this for 31 of the 48 invoices. The model wasn't being careless. The real problem was
> something else entirely: on the other 17 invoices the VAT number is printed in the footer, and our PDF renderer cut
> the footer off. Once we rendered the full page, all 48 came through.

After:

> ## Our renderer cut off the VAT numbers
>
> In the third attempt we asked the model to copy the VAT number from each invoice. It found the number on only 31 of
> the 48. It wasn't the model's mistake. On the other 17 the number is printed in the footer, and our PDF renderer cut
> the footer off. Once we rendered the full page, the model found all 48.

The opening of an announcement, before:

> We're thrilled to announce that Aigentic now supports workflows! This powerful new feature unlocks seamless
> multi-agent orchestration, empowering teams to build robust AI solutions.

After:

> Aigentic can now chain agents into a workflow. The output type of one agent is the input type of the next, and the
> compiler checks that they match. Branching isn't supported yet.
