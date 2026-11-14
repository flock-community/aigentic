---
title: "Steel Certificates at 99.9%: Optimizing the Extraction with Aigentic Platform"
slug: /steel-certificates
description: How Aigentic Platform took a light model to 99.9% correct on scanned steel certificates, by annotating with a frontier model and experimenting on the extraction
authors: [wilmveel]
tags: [AI, Aigentic, Aigentic Platform, Evaluations, Experiments, MCP, Document Extraction]
image: /img/blog/steel-certificates.png
---

# Steel Certificates at 99.9%: Optimizing the Extraction with Aigentic Platform

## The problem with steel certificates

Every batch of steel that a mill melts gets a heat number. The mill rolls the batch into coils and sends an inspection
certificate along with each coil, which says what the steel is made of. If a customer later wants to know which
certificate belongs to their product, you need the heat number to find it.

The certificates themselves are the problem. They come in as scanned PDFs from mills in many countries. Each mill uses
its own layout and language, some pages have handwriting on them, and you can't search the text because there isn't
any.

<!-- truncate -->

## What our client needed

One of our clients is a steel trader. They buy coils, process them and sell the result. Looking up certificates by
hand kept about one full-time employee busy.

They asked us for something that could:

- Read scanned certificates from any mill, in any layout or language
- Extract the heat and coil numbers, plus the data around them
- Stay affordable when every incoming certificate is read
- Tell them how accurate the extraction is

## The proof of concept

We built an extraction agent with Aigentic. It sends the scanned PDF to a multimodal model and asks for structured
output, so we always get the same fields back: issuer, certificate number, heats, coils and steel grade.

We started with a light model, because the client will read a lot of certificates and the cost per document adds up.
We also kept the first prompt simple. All runs were published to Aigentic Platform, including the document, the output
and the cost.

The first results looked good. But a heat number with one wrong character looks exactly like a correct one, so looking
at the output didn't tell us much. We needed to measure it.

## Annotating with a frontier model

On Aigentic Platform you measure an agent with an evaluation. You select a set of runs and the fields you care about,
and then you annotate: for each value you say whether it is correct, and if it isn't, what it should be. Later versions
of the agent are scored against those annotations.

Annotating certificates by hand takes a lot of time, so we had a frontier model do most of it. Aigentic Platform has an
MCP server, and through it an assistant like Claude can open a certificate, compare each extracted value with the scan
and save the annotation.

We still had to make the domain decisions ourselves. One certificate had a crate number where we expected a coil
number, and the agent had put the crate number in the coil field. We checked how this works in practice. A crate holds
cut sheets and a coil is cut into many sheets, so a crate number can't be a coil number. That became a rule the
assistant applied to all other certificates.

Several features that make this easier are being added to the platform:

- An AI-assisted evaluation has a checklist that walks you through connecting the assistant, calibrating on a few
  documents, annotating the rest and reviewing
- A review queue lists the assistant's annotations that a person still has to confirm, with the uncertain ones on top
- The document shows a box around each value the assistant read, so you can check a field without searching for it
- If a value is only formatted differently, the assistant suggests a normalizer that you can apply with one click

## Experimenting with the extraction

Once the annotations were done we had our first real score. The first version got 86% of the heat numbers right.

From there we let the platform's fix assistant work on it. The fix assistant reads the evaluation, looks for a pattern
in the errors, changes the agent's instructions and runs the new version on the same certificates. Each version gets a
score, so you can see whether a change helped. Some fixes had nothing to do with the prompt. The model kept reading an
8 as a 0 in small print, and that went away when we let it read the pages at a higher resolution.

This is also where the new Experiments page helps. It shows the versions of an agent as a tree, so you can see which
version a change was based on. Each version shows its score on the evaluation you pick and the difference with the
version before it. Everything the fix assistant tried is kept, including the reason it tried it. When you have the
version you want, you can copy it into your agent as Kotlin.

## Results

The version we ended up with extracts 99.9% of the core fields correctly. It still runs on the light model.

## Which model for which job

What we learned is that the two kinds of model are good for different parts of the work. A frontier model is worth its
cost for annotating and for analysing why an extraction goes wrong, because you only do that on a sample. The light
model does the bulk, which is reading every certificate that comes in.

The annotations are what make this work. They were made by a frontier model and checked by a person, and every version
of the light model is scored against them.

## What's next

We are now turning the proof of concept into a portal for our client, where employees, partners and customers can look
up a certificate by heat number. The AI-assisted evaluations and the Experiments page are still in development and
will be released in Aigentic Platform soon.
