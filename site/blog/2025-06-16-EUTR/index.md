---
title: "Leveraging AI for EUTR Compliance: How Aigentic Revolutionized Product Catalog Classification"
slug: /ai-eutr-compliance
description: Using Aigentic to automate EUTR compliance for product catalogs
authors: [nsmnds, wilmveel]
tags: [AI, EUTR, Compliance, Aigentic, Product Classification]
image: /img/blog/eutr.png
---

# Leveraging AI for EUTR Compliance: How Aigentic Revolutionized Product Catalog Classification

## The Challenge of EUTR Compliance for Product Catalogs

The European Union Timber Regulation (EUTR) represents a significant compliance challenge for retailers and manufacturers dealing with timber-based products. Implemented in 2013, this regulation prohibits placing illegally harvested timber on the EU market and requires operators to exercise due diligence in their supply chains. For retailers with extensive product catalogs containing thousands or even millions of items, manually classifying which products fall under EUTR jurisdiction is an overwhelming task.

<!-- truncate -->

## The Need for an Automated Solution

One of our clients, a major Dutch online retailer with over 35,000,000 products in their catalog, approached us with this exact problem. They needed to identify which products in their extensive catalog contained timber or timber-derived materials and therefore required EUTR compliance documentation. 

The client needed a solution that could:
- Automatically analyze product descriptions and specifications
- Accurately identify products containing timber components
- Scale to handle their entire product catalog
- Provide consistent classification results
- Adapt to new product categories over time

## Building an AI Solution with Aigentic

To address this challenge, we developed a custom AI solution using our Aigentic library. Aigentic provided the perfect foundation for this project, offering a powerful Kotlin Multiplatform DSL for building and integrating AI agents tailored to specific business needs.

Our approach involved creating a specialized classification agent that could analyze product data and determine EUTR applicability with high accuracy. The solution leveraged several key components of the Aigentic ecosystem:

## The Data Validation Challenge

One of the most significant challenges in the project was validating the data and ensuring the accuracy of the AI classifications. This is where the Aigentic platform proved particularly valuable.

We implemented a multi-stage validation process:

1. **Initial Training**: We started with a small set of pre-classified products to train our initial model
2. **Iterative Labeling**: Using the Aigentic platform, we created an interface for experts to review and correct AI classifications
3. **Golden Dataset Creation**: Through this process, we gradually built a "golden dataset" of correctly classified products
4. **Continuous Improvement**: The system continuously learned from expert feedback, improving its accuracy over time

The Aigentic platform's ability to facilitate human-in-the-loop validation was crucial for building trust in the system. Product managers and compliance officers could review classifications, provide feedback, and see how the system improved over time.

## Looking Forward: Complete EUTR Classification

Based on the success of this project, it appears that Aigentic can be deployed to provide complete EUTR classification for product catalogs of any size. The solution we developed is not just a one-time fix but a sustainable approach to ongoing compliance.

As regulations evolve and product catalogs grow, AI-powered classification using Aigentic offers a scalable, accurate, and efficient solution. The combination of powerful language models with human expertise creates a system that continuously improves while maintaining the critical human oversight necessary for regulatory compliance.

For retailers and manufacturers struggling with EUTR compliance, AI-powered solutions built on platforms like Aigentic represent the future of regulatory technology – turning what was once an overwhelming burden into a streamlined, reliable process.
