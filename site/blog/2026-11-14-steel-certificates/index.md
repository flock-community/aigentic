---
title: "Reading Steel Certificates: Expensive Models to Judge, Cheap Models to Run"
slug: /steel-certificates
description: How we used Aigentic Platform to get a light model to 90.9% correct on scanned steel certificates, by annotating with a strong model and experimenting on the prompt
authors: [wilmveel]
tags: [AI, Aigentic, Aigentic Platform, Evaluations, Experiments, MCP, Document Extraction]
image: /img/blog/steel-certificates.png
---

# Reading Steel Certificates: Expensive Models to Judge, Cheap Models to Run

A steel trader asked us a simple question: can AI read our certificates? A few weeks later we had a proof of concept
that finds the right certificate by its heat number, running on a light model that gets 90.9% of the extraction right.

This post is about how we got there. Not by writing a clever prompt, but by letting Aigentic Platform measure every
prompt we tried, and by using an expensive model where it pays off: judging the output, not producing it.

<!-- truncate -->

## The certificate has to travel along

Steel starts at a mill. Every batch that is melted there gets a **heat number**, and that number follows the steel for
the rest of its life. The mill rolls the batch into coils and ships each coil with an inspection certificate that
states what the steel is made of.

![From the mill to the customer, and the certificate has to travel along](/img/blog/steel-certificates.png)

Our client buys those coils, embosses and cuts them, and sells the result. When a customer asks which certificate backs
their product, the answer runs through the heat number: it is the one reference that links a sheet of steel back to the
batch it was melted in.

In practice, that link is hard to follow. The certificates arrive as scanned PDFs, mostly by email, from many mills.
Each mill has its own layout and its own language. Some pages have handwriting on them, and many have no searchable
text at all. Finding the right certificate was manual work, and a lot of it.

## Unstructured in, structured out

For an agent this is a classic extraction task: a PDF goes in, structured data comes out. With Aigentic you describe
that output as a data class, and the model has to answer in that shape:

```kotlin
@AigenticParameter
data class Certificate(
    val certificateNumber: String,
    val issuer: String,
    val items: List<CertificateItem>,
)

@AigenticParameter
data class CertificateItem(
    val heatNumber: String,
    val coilNumber: String,
    val steelGrade: String,
)
```

We started with a wide variety of certificates, from as many mills as we could get, and ran them all through a first
version of the agent. Two choices we made on purpose:

- **A very light model.** If the bulk of the certificates runs on a cheap model, reading every certificate that comes
  in stays affordable.
- **A first prompt we did not polish.** We let AI write it. It was a starting point to measure, not something to get
  right by hand.

Every run was published to Aigentic Platform: the PDF, the model's requests, the output and the cost. The first results
looked fine at a glance, which is exactly the trap. A heat number that is one character off looks just as convincing as
a correct one.

## First, decide what correct means

You can't improve what you don't measure, so using Aigentic Platform always starts with an **annotation phase**. You
put runs in an evaluation, pick the fields that matter, and say for each field whether the agent got it right. Where it
didn't, you fill in the value the document states. That is your ground truth. From then on, every version of the agent
gets a score per field instead of an impression.

Annotating is also the tedious part. Opening certificate after certificate and hunting for a heat number in a dense
scan takes time, and most of that time goes into finding the value, not judging it.

### Let an expensive model do the first pass

Aigentic Platform is also an MCP server, so an assistant such as Claude can do what a user can, including annotating.
That is what makes the next step work: we let a strong, expensive model read the certificates and correct the light
model's output.

The newest work on the platform turns this from a trick you had to know into a guided flow:

- **AI-assisted evaluations.** Choose "AI-assisted" when you create an evaluation and its page shows a checklist:
  connect Claude, let it annotate a few inputs to calibrate, review those, let it annotate the rest, review again. The
  steps tick themselves as the platform sees the work happen.
- **An annotation playbook.** The platform serves the instructions for annotating as an MCP prompt: how to verify a
  value against the source, when a field counts as absent, which model and effort to use, and helper scripts such as
  one that crops and enlarges a part of a page so small print becomes readable. Everyone uses the latest version,
  without copying prompts around.
- **Notes per agent.** What you correct during calibration is written down as notes on the agent, for example how a
  certain mill writes its heat numbers. The assistant reads them before it annotates the rest.
- **A review queue.** The assistant's annotations wait in one queue until a person confirms them, starting with the
  fields it was unsure about and the ones where it says the agent was wrong.
- **Boxes on the document.** The assistant marks where it read each value. Click a field and the PDF zooms in on that
  spot, so checking a heat number takes a glance.
- **Suggested normalizers.** When a value only differs in formatting, say a heat number with or without a dash, the
  assistant suggests a normalizer for that field. You apply it with one click, or dismiss it.

You stay in charge of what correct means. The assistant does the reading, and you check the cases that need you.

## Then, experiment

With ground truth in place, the light model's first prompt got an honest score. Then we gave the platform the freedom
to improve it.

The **fix assistant** looks at an evaluation, finds a pattern in what goes wrong, changes the agent's task and runs the
new version on the same certificates. Each try is scored against the ground truth straight away. A try that helps is
built on. A try that makes things worse is left behind, and the assistant goes back to an earlier version and tries
something else.

This is where the upcoming **Experiments** page comes in:

- **Versions form a tree.** Each version shows which version it came from and whether it is in production, an
  experiment, or abandoned.
- **Every version is scored.** Pick an evaluation and each version shows its accuracy, how many points it gained or
  lost against the version it came from, and how many inputs it ran on.
- **Nothing the assistant tries is lost.** Every try is kept as an experiment, with the reason it was tried. A later
  session builds on it instead of starting over, and a session that fails halfway keeps what it found.
- **Use in code.** When a version is the one you want, the platform gives you its task as Kotlin to copy into your
  agent. Once production runs it unchanged, the experiment becomes a production version in the tree.

In several steps, the assistant took the light model's extraction to **90.9% correct**. We didn't tune the prompt by
hand in any of those steps. We looked at the scores and at the reason behind each try.

## Expensive models to judge, cheap models to run

The main thing we took away from this project is where to spend your tokens.

- **Use the expensive model for analysing.** Building ground truth and working out why an extraction fails are hard
  tasks, and you do them once, on a sample.
- **Use the cheap model for the bulk.** Reading every certificate that comes in happens all day, every day, so that is
  where the cost per document counts.

The corrections are what connect the two. Because the strong model's annotations are checked by a person and stored as
ground truth, every prompt for the cheap model can be measured against them. You get the judgment of an expensive model
at the price of a cheap one, and a number that tells you how close you are.

## What's next

For our client, the proof of concept is growing into a portal where employees, partners and customers find a
certificate by its heat number.

For Aigentic Platform, the AI-assisted evaluations and the Experiments page described here are being finished right
now, and will land in the platform soon. If you extract structured data from documents and want to know how good your
agent really is, this is the way of working we'd recommend: annotate first, experiment second, and let the scores
decide.
