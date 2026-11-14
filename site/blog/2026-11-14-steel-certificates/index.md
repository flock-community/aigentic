---
title: "Steel Certificates at 99.9%: Optimizing the Extraction with Aigentic Platform"
slug: /steel-certificates
description: How Aigentic Platform took a light model to 99.9% correct on scanned steel certificates, by annotating with a frontier model and experimenting on the extraction
authors: [wilmveel]
tags: [AI, Aigentic, Aigentic Platform, Evaluations, Experiments, MCP, Document Extraction]
image: /img/blog/steel-certificates.png
---

# Steel Certificates at 99.9%: Optimizing the Extraction with Aigentic Platform

## The Challenge of Tracing Steel Certificates

Steel starts at a mill. Every batch that is melted there gets a **heat number**, and that number follows the steel for
the rest of its life. The mill rolls the batch into coils and ships each coil with an inspection certificate that
states what the steel is made of. When a customer asks which certificate backs their product, the answer runs through
the heat number.

In practice, that link is hard to follow. Certificates arrive as scanned PDFs from mills all over the world, each with
its own layout, in several languages, some with handwriting on them. There is no text to search, so finding the right
certificate is manual work.

<!-- truncate -->

## The Need for an Automated Solution

We helped one of our clients, a steel trader that buys coils, processes them and sells the result, with this exact
problem. Finding certificates by hand took them about one full-time employee.

They needed a solution that could:

- Read scanned certificates from any mill, whatever the layout or language
- Accurately extract the heat and coil numbers, and the data around them
- Stay affordable when every incoming certificate is read
- Show how accurate the extraction really is

## Building an AI Solution with Aigentic: The POC Approach

For this POC, we built an extraction agent with Aigentic. The scanned PDF goes straight to a multimodal model, and
Aigentic's structured output makes sure the answer always comes back in the same shape: issuer, certificate number,
heats, coils, steel grade.

We chose a light model on purpose, so reading every certificate stays affordable. And we did not polish the first
prompt by hand. Every run was published to Aigentic Platform, with its document, its output and its cost, so we could
measure the agent instead of guessing.

That turned out to be necessary. The first results looked right, but a heat number with one wrong character looks just
as convincing as a correct one.

## Building Ground Truth with a Frontier Model

Working with Aigentic Platform always starts with an annotation phase. You put runs in an evaluation, pick the fields
that matter, and mark for each value whether the agent got it right. That ground truth is what every later version of
the agent is scored against.

Annotating is also the slow part, so we let a frontier model do the reading. Aigentic Platform is an MCP server, which
means an assistant such as Claude can open each certificate, check every value against the scan and save the
annotation. A person stays in charge of what correct means. One certificate, for example, had no coil number, only a
crate number, and the agent had filled that in. Is a crate a coil? No: one coil yields many sheets, never the other
way around. We decided that once, and it was written down as a rule for every later annotation.

The newest work on the platform makes this a guided flow:

- **AI-assisted evaluations**: A checklist takes you from connecting the assistant to calibrating, annotating and
  reviewing
- **Review queue**: The assistant's annotations wait for a person to confirm them, starting with the ones it was
  unsure about
- **Value locations**: Boxes on the document show where each value was read, so checking a field takes a glance
- **Suggested normalizers**: When a value only differs in formatting, the assistant suggests the fix and you apply it
  with one click

## Experimenting Towards a Better Extraction

With ground truth in place, the first version got an honest score: 86% of the heat numbers correct. Then we gave the
platform the freedom to optimize.

The platform's fix assistant looks at an evaluation, finds a pattern in what goes wrong, changes the agent's
instructions and scores the new version on the same certificates. A change that helps is built on. A change that
hurts is left behind. Not every improvement was a matter of wording: the model sometimes read an 8 as a 0 in small
print, and letting it look at each page in higher resolution solved that.

The upcoming Experiments page shows this process at a glance:

- **Version tree**: See which version came from which, and whether it is in production, an experiment, or abandoned
- **Scores per version**: Every version shows its accuracy and what it gained or lost
- **Nothing is lost**: Every change the fix assistant tries is kept, with the reason it was tried
- **Use in code**: Copy the best version into your agent as Kotlin

## Results

The version the fix assistant proposed extracts 99.9% of the core fields correctly, still on the light model. Because
that number comes from an evaluation against checked ground truth, it is a measurement and not an impression.

## Frontier Models to Judge, Light Models to Run

The main lesson from this POC is where to use which model. Building ground truth and working out why an extraction
fails are hard tasks that you do once, on a sample, so that is where a frontier model pays off. Reading every incoming
certificate is the bulk of the work, and that runs on a light model.

Aigentic Platform connects the two. The frontier model's annotations become ground truth, and every version of the
light model is measured against them.

## Looking Forward: From POC to Certificate Portal

For our client, the POC is growing into a portal where employees, partners and customers find a certificate by its
heat number. The AI-assisted evaluations and the Experiments page are being finished right now and will land in
Aigentic Platform soon. For anyone who extracts structured data from documents, the approach carries over: annotate
first, experiment second, and let the scores decide.
