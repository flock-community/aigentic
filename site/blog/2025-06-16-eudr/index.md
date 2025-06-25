---
title: "Leveraging Aigentic for EUDR Compliance: A Proof of Concept"
slug: /ai-eudr-compliance
description: A Proof of Concept using Aigentic to automate EUDR compliance for product catalogs
authors: [nsmnds, wilmveel]
tags: [AI, EUDR, Compliance, Aigentic, Product Classification]
image: /img/blog/eudr.png
---

# Leveraging Aigentic for EUDR Compliance: A Proof of Concept

## The Challenge of EUDR Compliance for Product Catalogs

The EU Regulation on Deforestation-free Products (EUDR) represents a significant compliance challenge for retailers and manufacturers. This regulation, which came into force on June 29, 2023, aims to minimize the EU's contribution to global deforestation and forest degradation. It prohibits placing products linked to deforestation on the EU market and requires operators to implement robust due diligence systems. The regulation covers cattle, cocoa, coffee, oil palm, rubber, soya, and wood, along with derived products like leather, chocolate, and furniture. For retailers with extensive product catalogs containing thousands or even millions of items, manually classifying which products fall under EUDR jurisdiction is an overwhelming task.

<!-- truncate -->

## The Need for an Automated Solution

One of our clients, a major Dutch online retailer with over 35,000,000 products in their catalog, approached us with this exact problem. They needed to identify which products in their extensive catalog contained EUDR-regulated commodities (cattle, cocoa, coffee, oil palm, rubber, soya, and wood) or their derived products, and therefore required EUDR compliance documentation.

The client needed a solution that could:
- Automatically analyze product descriptions and specifications
- Accurately identify products containing EUDR-regulated commodities and their derivatives
- Scale to handle their entire product catalog
- Provide consistent classification results
- Adapt to new product categories over time

## Building an AI Solution with Aigentic: The POC Approach

To address this challenge, we developed a Proof of Concept (POC) using our Aigentic library. Aigentic provided the perfect foundation for this POC, offering a powerful Kotlin Multiplatform DSL for building and integrating AI agents tailored to specific business needs.

For this POC, our approach involved creating a specialized classification agent that could analyze product data and determine EUDR applicability with high accuracy. The proof of concept leveraged several key components of the Aigentic ecosystem to demonstrate the potential of this approach:

## The Data Validation Challenge in Our POC

One of the most significant challenges in the POC was validating the data and ensuring the accuracy of the AI classifications. This is where Aigentic proved particularly valuable for our proof of concept. By manually validating the data, we were able to identify and correct any errors that were introduced during the classification process. Aigentic gave us the ability to review and correct the output of the AI agent, providing a human-in-the-loop validation process that was essential for the POC.

We implemented a multi-stage validation process:

1. **Initial Training**: We started with a small set of products to classify them with our initial agent
2. **Iterative Labeling**: Using Aigentic, we created an interface through Excel for experts to review and correct AI classifications
3. **Golden Dataset Creation**: Through this process, we gradually built a "golden dataset" of correctly classified products
4. **Continuous Improvement**: The system continuously learned from expert feedback, improving its accuracy over time

Aigentic's ability to facilitate human-in-the-loop validation by using Excel was crucial for building trust in the system. During the proof of concept, stakeholders could review classifications, provide feedback, and see how the system improved over time, which was essential for validating the approach.

## Looking Forward: From POC to Complete EUDR Classification

With EUDR compliance deadlines approaching, organizations need to act quickly to prepare their due diligence systems. Based on the success of this POC, we are planning to deploy Aigentic for complete EUDR classification for product catalogs of any size. The POC has demonstrated that our approach can be developed into a sustainable solution for ongoing compliance that helps companies meet these critical deadlines.

As regulations evolve and product catalogs grow, our POC suggests that AI-powered classification using Aigentic could offer a scalable, accurate, and efficient solution. The POC demonstrated how the combination of powerful language models with human expertise can create a system that continuously improves while maintaining the critical human oversight necessary for regulatory compliance.

For retailers and manufacturers struggling with EUDR compliance, our POC demonstrates that AI-powered solutions built on platforms like Aigentic have the potential to transform regulatory compliance – turning what was once an overwhelming burden into a streamlined, reliable process. Based on the success of this POC, we are now proceeding with the development of a full-scale solution.
