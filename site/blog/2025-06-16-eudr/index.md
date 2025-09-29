---
title: "Leveraging Aigentic for EUDR Compliance"
description: A solution using Aigentic to automate EUDR compliance for product catalogs
authors: [nsmnds, wilmveel]
tags: [AI, EUDR, Compliance, Aigentic, Product Classification]
image: /img/blog/eudr.png
---

# Leveraging Aigentic for EUDR Compliance

## The Challenge of EUDR Compliance for Product Catalogs

The EU Regulation on Deforestation-free Products (EUDR) represents a significant compliance challenge for retailers and
manufacturers. This regulation, which came into force on June 29, 2023, and becomes enforceable on December 30, 2025,
aims to minimize the EU's contribution to global deforestation and forest degradation. It prohibits placing products
linked to deforestation on the EU market and requires operators to implement robust due diligence systems. The
regulation covers cattle, cocoa, coffee, oil palm, rubber, soya, and wood, along with derived products like leather,
chocolate, and furniture. For retailers with extensive product catalogs containing thousands or even millions of items,
manually classifying which products fall under EUDR jurisdiction is an overwhelming task.

## The Need for an Automated Solution

We helped one of our clients, a major Dutch online retailer with over 15 million products in their catalog, with this
exact problem. They needed to identify which products in their extensive catalog needed to be EUDR compliant. EUDR
compliance is determined by specific Harmonized System (HS) codes that classify products containing the regulated
commodities, making accurate product classification essential for compliance.

They needed a solution that could:

- Automatically analyze product descriptions and specifications
- Accurately identify the HS-code for each product in their catalog
- Scale to handle their entire product catalog
- Generate accurate LLM token cost estimates
- Provide consistent classification results

## Building an AI Solution with Aigentic: The POC Approach

To address this challenge, we developed a solution using our Aigentic library. Aigentic provided the
foundation for this POC, offering a powerful Kotlin Multiplatform DSL for building and integrating AI agents tailored to
specific business needs.

For this POC, our approach involved creating a specialized classification agent that could analyze product data and
determine the HS-code with high accuracy. The solution leveraged several key components of the Aigentic
ecosystem to demonstrate the potential of this approach.

## The Data Validation Challenge in the POC

One of the most significant challenges in the POC was validating the data and ensuring the accuracy of the AI
classifications. This is where Aigentic proved particularly valuable for our solution. By manually validating
the data, we were able to identify and correct errors that were introduced during the classification process.
Aigentic allowed us to review and correct the output of the AI agent, providing a human-in-the-loop validation process
that was essential for the POC.

We implemented a multi-stage validation process:

- **Initial exploration**: We started with a small set of products to explore feasibility
- **Iterative Labeling**: Using Aigentic, we created an interface through Excel to review and correct AI classifications
- **Golden Dataset Creation**: Through this process, we gradually built a "golden dataset" of correctly classified
  products
- **Continuous Improvement**: The agent was continuously adapted based on feedback, improving its accuracy over time

Aigentic's ability to combine human-in-the-loop validation with AI automation was crucial for building trust in the
system. During the solution, stakeholders could review classifications, provide feedback, and see how the system
improved over time, which was essential for validating the approach.

## Operational Observability: Monitoring Performance and Costs in the POC

A critical aspect of any AI-powered solution is understanding its operational characteristics. Aigentic provided
comprehensive observability features that were essential for monitoring and optimizing our EUDR classification POC.
Through Aigentic Platform's built-in monitoring, we gained visibility into key metrics:

- **Cost Tracking**: Allowing us to provide accurate cost projections for scaling to the full product catalog
- **Latency Monitoring**: Ensuring the solution could meet real-world requirements
- **Run Logging**: Capturing every classification decision and reasoning, creating a complete trail essential for
  quality assurance

This observability enabled us to fine-tune performance and build confidence in the solution's reliability—critical
factors for successful implementation.

## Results

The solution demonstrated promising results. The agent achieved 98% accuracy on the golden dataset. Statistical
analysis with a 95% confidence interval suggests that when applied to the complete product catalog, the real accuracy
would likely fall between 96.77% and 99.23%, indicating robust performance suitable for large-scale EUDR compliance
operations.

## Looking Forward: From POC to Complete EUDR Classification

With EUDR compliance deadlines approaching, organizations need to act quickly to prepare their due diligence systems.
The POC has demonstrated that our approach can be developed into a solution for ongoing compliance that helps companies
meet these critical deadlines.

As regulations evolve and product catalogs grow, the POC suggests that AI-powered classification using Aigentic could
offer a scalable, accurate, and efficient solution. The POC demonstrated how the combination of powerful language models
with human expertise can create a system that continuously improves while maintaining the critical human oversight
necessary for regulatory compliance.
