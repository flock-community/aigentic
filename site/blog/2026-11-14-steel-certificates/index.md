---
title: "Steel Certificates at 99.9%: Optimizing the Extraction with Aigentic Platform"
slug: /steel-certificates
description: How Aigentic Platform took a light model to 99.9% correct on scanned steel certificates, by annotating with a frontier model and experimenting on the extraction
authors: [wilmveel]
tags: [AI, Aigentic, Aigentic Platform, Evaluations, Experiments, MCP, Document Extraction]
image: /img/blog/steel-certificates.png
---

# Steel Certificates at 99.9%: Optimizing the Extraction with Aigentic Platform

## Steel certificates

A steel mill gives every batch of steel a heat number. The mill makes coils from the batch. Each coil comes with an
inspection certificate that describes the steel. To find the certificate for a product, you need the heat number.

The certificates are scanned PDFs. They come from many mills, and each mill has its own layout and language. Some pages
have handwriting on them. The text in a scan cannot be searched.

<!-- truncate -->

## What our client needed

One of our clients is a steel trader. They buy coils, process them and sell the result. Finding certificates by hand
was about one full-time job.

They asked for a solution that can:

- Read scanned certificates from any mill
- Extract the heat number, the coil number and the other data
- Read every new certificate at a low cost
- Show how accurate the extraction is

## The proof of concept

We built an agent with Aigentic. The agent sends the scanned PDF to a model that can read images. The model returns
structured output, so we always get the same fields: issuer, certificate number, heats, coils and steel grade.

We started with a light model, because the client has many certificates to read. We kept the first prompt simple. Each
run was sent to Aigentic Platform with the document, the output and the cost.

The first results looked fine. But you cannot see if a heat number is correct without checking it against the scan. So
we needed a way to measure the results.

## Annotating with a frontier model

In Aigentic Platform you measure an agent with an evaluation. You choose a set of runs and the fields you want to
check. Then you annotate each value: is it correct, and if not, what is the correct value? New versions of the agent
are scored against these annotations.

Annotating by hand takes a lot of time. So we used a frontier model for it. Aigentic Platform has an MCP server. With
it, an assistant like Claude can open a certificate, compare each value with the scan and save the annotation.

Some questions needed a person. On one certificate the agent used a crate number as the coil number. We looked into
it. A crate holds sheets that are cut from a coil, so a crate number is not a coil number. We wrote this down as a
rule, and the assistant used it for the other certificates.

New features in the platform make this easier:

- A checklist that guides you through the steps of an AI-assisted evaluation
- A review queue with the annotations that a person still needs to check
- Boxes on the document that show where each value was found
- Suggestions for a normalizer when a value only has a different format

## Experimenting with the extraction

With the annotations we could score the first version. It had 86% of the heat numbers correct.

Next, we used the fix assistant in the platform. It looks at the errors in an evaluation, changes the instructions of
the agent and runs the new version on the same certificates. Each version gets a score, so you can see if a change
helped.

Not every fix was a change to the prompt. The model sometimes read an 8 as a 0 in small print. Reading the pages at a
higher resolution fixed this.

The new Experiments page shows all versions of an agent as a tree. For each version you see its score and the
difference with the version before it. All changes the fix assistant tried are saved, with the reason for each change.
You can copy the best version into your agent as Kotlin code.

## Results

The final version extracts 99.9% of the core fields correctly. It still uses the light model.

## Which model for which job

We used two kinds of model for two kinds of work. The frontier model annotates and analyses the errors. This is only
needed for a small set of documents. The light model reads all the certificates that come in.

The annotations connect the two. A frontier model makes them and a person checks them. Each version of the light model
is scored against them.

## What's next

We are now building a portal for our client. Employees, partners and customers can use it to find a certificate by its
heat number. The AI-assisted evaluations and the Experiments page are still in development. They will be available in
Aigentic Platform soon.
