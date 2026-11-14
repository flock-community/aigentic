---
title: "A Crate Is Not a Coil: Teaching a Light Model to Read Steel Certificates"
slug: /steel-certificates
description: The story of a proof of concept that reads scanned steel certificates, and how Aigentic Platform took a light model to 99.9% correct, one experiment at a time
authors: [wilmveel]
tags: [AI, Aigentic, Aigentic Platform, Evaluations, Experiments, MCP, Document Extraction]
image: /img/blog/steel-certificates.png
---

# A Crate Is Not a Coil: Teaching a Light Model to Read Steel Certificates

It started with a folder of PDFs and a simple question from a steel trader: can AI read these?

A few weeks later a light model read them with 99.9% of the core fields correct. The first version took an
afternoon. Everything after that is the actual story: a crate that is not a coil, a model that would rather write
nonsense than say "nothing here", and an 8 that kept turning into a 0.

<!-- truncate -->

## One number that follows the steel

Steel starts at a mill. Every batch that is melted there gets a **heat number**, and that number follows the steel for
the rest of its life. The mill rolls the batch into coils and ships each coil with an inspection certificate that
states what the steel is made of.

Our client buys those coils, embosses and cuts them, and sells the result. When a customer asks which certificate backs
their product, the answer runs through the heat number. It is the one reference that links a sheet of steel back to the
batch it was melted in.

Then we opened the folder. Every certificate was a scan, without any text you can select or search. They came from
mills all over the world, each with its own layout, in five languages, some with handwriting on them. Finding the right
one for a customer was manual work, and it took about one full-time employee.

## The afternoon version

We skipped OCR. Turning a scan into text first throws away the table layout and the handwriting, and those are the hard
parts of these documents. So the PDF itself goes to a multimodal model, and with Aigentic the model has to answer in a
fixed shape:

```kotlin
@AigenticParameter
data class Certificate(
    val certificateNumber: String?,
    val issuer: String?,
    val heats: List<Heat>,
    val items: List<CertificateItem>,
)

@AigenticParameter
data class CertificateItem(
    val heatNumber: String,
    val coilNumber: String?,
    val steelGrade: String?,
)
```

We picked a light model on purpose, Gemini Flash. A certificate costs a few cents to read that way, and that matters
when every certificate that comes in has to be read. We also didn't polish the first prompt. It was something to
measure, not something to get right by hand.

By the end of the afternoon there was a table on screen: mill, certificate, heat, coil, grade. It looked done.

It wasn't, and we couldn't see it. A heat number with one wrong character looks just as convincing as a correct one.
So every run went to Aigentic Platform, with its PDF, its output and its cost, and we started on the part that tells
you how good an agent really is.

## What is a coil number, anyway?

On Aigentic Platform you always annotate first. You put runs in an evaluation, pick the fields that matter, and say
for each value whether the agent got it right. Where it didn't, you fill in what the document states. That is your
ground truth, and from then on every version of the agent gets a score instead of an impression.

For some twenty certificates that means reading 28 heat numbers and 38 coil numbers off dense scans. We didn't do that
ourselves. Aigentic Platform is also an MCP server, so an assistant can do what a user can. We let Claude, a far more
expensive model than the one doing the extraction, do the reading: render each page, crop and enlarge the right table
cell, read every character, and save the annotation with a note on where it found the value.

It was good at reading. What it could not do was decide what a value *means*, and those turned out to be the
interesting cases:

- One mill prints two codes in the heat cell, a full one with a shorter form of it on the line below. The agent joined
  them into one value. Which one is the heat number?
- One certificate has no coil column at all, only a crate number, and the agent put the crate number in the coil
  field. Is that wrong? A crate is a package of cut sheets. One coil yields many sheets, never the other way around.
  So no: a crate is not a coil, and the right answer is that there is no coil number.
- Another certificate has no coil column either, but a test number printed next to the heat. That one does identify
  the coil.

These are decisions for a person who knows the domain. Claude collected the open questions, we answered them once, and
each answer was written down as a rule, so the next session annotates the same way.

With the ground truth in place, the afternoon version finally got an honest score. Heat numbers: 20 out of 23, or 86%.

## One change at a time

Now we could experiment. The rules were simple: one change per experiment, run it on the certificates that fail plus a
few that are right today, and compare on the same documents.

**The first experiment** was the heat cell with two codes. One instruction: the heat number is the full code on the
first line, never join the lines. The failing certificate was fixed, the controls stayed right, and on the whole set
the score went to 27 out of 28.

There was one surprise along the way. In the full run a different certificate suddenly came back wrong. We ran it three
more times and it was right every time. That is when it sank in that every run is a sample. The model is not
deterministic, so one run per document proves nothing on its own. From then on, anything that changed got re-run before
we believed it.

**The second experiment** failed, and it taught us the most. We told the model that a crate number is not a coil
number, and to return null when no coil number is printed. In four runs it did that once. The other three times it
wrote the name of the next field into the coil number, or the steel grade, and every field after it shifted one place.

A new wording would not have fixed that. The model wasn't misunderstanding the rule. It was struggling to say "nothing
here" at all. The cause was in our own library: Aigentic marked optional fields as not required, but never told Gemini
that null was an allowed answer. We fixed that in Aigentic, and the crate numbers came back as null without any
instruction.

**The third experiment** cleaned up after the second. Now that the model could say null, it used it too eagerly: the
certificate with a test number instead of a coil column lost its coil number in four out of four runs. One more rule,
straight from the annotation decisions, and the set was clean: 38 out of 38 coil numbers, 28 out of 28 heat numbers.

## Let the platform take over

That was a handful of experiments on two fields, run by hand. A certificate has many more: chemistry per heat,
mechanical test results, dimensions, weights, order and delivery numbers.

So for the full certificate we gave the platform the freedom to optimize. The **fix assistant** looks at an evaluation,
finds a pattern in what goes wrong, changes the agent's task, runs the new version on the same documents and scores it
against the ground truth. A change that helps is built on. A change that hurts is left behind.

Its ninth version was the one we shipped. Most of it was instructions like the ones we wrote by hand, only more of
them: which number is the customer's order number and which the mill's, to keep only the English half of bilingual
text, which unit a hardness value gets, when to leave a field empty.

One change was not about the prompt at all. In the small print of a scan the model read an 8 as a 0, and now and then
lost a decimal point. No instruction fixes that. Letting the model look at each page in higher resolution does, at
about twice the input tokens per page. It is still the light model, and it still costs cents per certificate.

That version scored **99.9% of the core fields correct**.

## What we did by hand is now in the platform

Looking back, most of our time went into things the platform could have done for us. Nearly everything we improvised
during this project is being built into Aigentic Platform right now:

- **AI-assisted evaluations.** Choose "AI-assisted" when you create an evaluation and its page shows a checklist:
  connect Claude, let it annotate a few inputs to calibrate, review those, let it annotate the rest, review again.
- **An annotation playbook.** Our instructions for annotating, including the script that crops and enlarges a part of
  a page, are served by the platform itself. Everyone gets the latest version without copying prompts around.
- **Notes per agent.** The crate rule and the two-codes rule are exactly the kind of thing you write down once. The
  assistant reads them before it annotates.
- **A review queue.** The assistant's annotations wait in one queue until a person confirms them, starting with the
  ones it was unsure about.
- **Boxes on the document.** The assistant marks where it read each value. Click a field and the PDF zooms in on that
  spot, so checking a heat number takes a glance.
- **Suggested normalizers.** When a value only differs in formatting, the assistant suggests a normalizer for the
  field, and you apply it with one click.
- **The Experiments page.** Versions form a tree, so you see which version came from which. Each one is scored on the
  evaluation you pick, with the points it gained or lost. Everything the fix assistant tries is kept, with the reason
  it was tried, so a failed experiment like our second one stays on record instead of being repeated.
- **Use in code.** When a version is the one you want, the platform gives you its task as Kotlin to copy into your
  agent.

## Strong models to judge, light models to run

The thing we'd tell anyone starting a project like this is where to spend your tokens.

- **Use the strong model for analysing.** Building ground truth and working out why an extraction fails are hard
  tasks, and you do them once, on a sample.
- **Use the light model for the bulk.** Reading every certificate that comes in happens all day, every day, so that is
  where the cost per document counts.

The corrections connect the two. The strong model's annotations are checked by a person and stored as ground truth, so
every version of the light model can be measured against them. You get the judgment of a strong model at the price of
a light one, and a number that tells you how close you are.

For our client, the proof of concept is growing into a portal where employees, partners and customers find a
certificate by its heat number. And the folder of PDFs nobody could search is now a table you can.
