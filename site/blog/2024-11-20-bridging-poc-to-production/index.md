---
title: "Streamlining LLM App Development for Real-World Applications"
slug: /bridging-poc-to-production
description: How Aigentic helps transform your LLM application from proof of concept to production
authors: [nsmnds]
tags: [LLM, AI, production, edge cases]
image: /assets/images/67657c01287e7dcbaf42e686_signal-2024-10-10-151503_002-4c6f9de9b8f1c7703364a1890f7f2c6f.jpeg
---

# Bridging the Gap: From LLM Proof of Concept to Production

## Introduction

Imagine this: You've just unveiled your AI application powered by a Large Language Model (LLM). The demo wows everyone, and you're riding high on success. Then reality hits. As real-world documents start flowing through your app, edge cases emerge, exposing gaps between your carefully crafted proof of concept and the demands of a production environment.

Sound familiar? You're not alone. In the fast-paced world of AI development, bridging the chasm between a promising demo and a robust, production-ready application is a challenge that plagues everybody. This gap often leads to stalled projects, missed opportunities, and frustrated stakeholders.

Enter Aigentic - a comprehensive platform designed to transform your LLM application journey from proof of concept to production. In this post, we'll explore how Aigentic empowers developers to tackle real-world challenges, ensuring your AI applications don't just impress in the demo room, but deliver consistent value in the wild.

Let's look at this challenge through a real-world example.

<!-- truncate -->

## Case Study: Invoice Processing Agent

We've created an LLM-powered agent to handle PDF invoices. The agent's job is to pull out key information like invoice numbers, customer numbers, items, and amounts. 

Here's a simple version of our agent, made using Aigentic:

![Invoice Processing Agent](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/6765788065d4228423aeda5c_AD_4nXfl7FtiC-gjqtRmVpXevRGJOWU6WShod_gJHb3UEl9vT4r9D9slrXeB_fcdMRt6Ec0QGNIZNUW0qnD41tRoZ8lFmt9xqSXarHwvyTh7hHqQt87Yw0P0V445wv_g_BmUlOjpcVHxahc1eSg44-SZMgPrAIg.png)

The agent has access to a tool called "saveInvoiceTotal". The agent can use this tool to interact with the "real-world", in this case by for example saving the invoice information to a database or sending it to an external service.

![Save Invoice Tool](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880604743e72ee6efe0_AD_4nXeT0ft94bqX-NbHePi8esgSjcirzLt5gGjAxyL9e28j2-qE82C0OYuHueguD3PRIbdOoZzdQ4KgGsyyWUzhnxEhNWW8s-AzvuO9441Mzf9ca2fyiHGJlXtcsGKSsavcSW2eswnZlrHPR7FhqO4ylUTeVlfG.png)

Our first tests with sample invoices look good.However, when we implement it in a real-world scenario, we hit a problem. Some invoices have summary rows that add up other items. We don't want these summary rows saved as separate items, as it would make our total amounts wrong:

![Invoice with Summary Rows](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880e8bd6c3ebb14097d_AD_4nXdSERgapMMS_jInVNtZMB7qImekye9tM32Kck2aZt81_UeY3HR03_Dt0Efcm9xKwYAe41Opyo9MWuDqyj_ZhNnYVIRJz5AQvXasQjgEyFKLAQ7y8bUc9vlyJyASGnYdpoRfoqWnEuJzY7PJck1PdqYhI9WF.png)

‍

In the screenshot above we can see that 2 lines: "Smart City Platform Implementation" and "Renewable Energy Integration Services" are included as invoice items, this is not what we want.

To prevent this issue from occurring an extra instruction could be added: "If the line items are summarized, please provide only the details of the invoice items". But how can we quickly test if this works? And how do we know if invoices that were already processed correctly are still being processed correctly? We want to prevent invoice lines from suddenly missing later because the agent starts seeing them unintentionally as summary lines.

## Enter Aigentic platform…

Because we've configured the Aigentic platform in our agent, all our runs are recorded in this platform. This provides us with the opportunity to replay these runs in a sandbox environment within Aigentic. We can now validate whether our adjustment is sufficient AND that our earlier invoices are still being processed correctly. Since the test runs completely isolated and the "saveInvoiceTool" isn't actually called in the test, we can iterate as many times as needed until it works, without the invoice being processed multiple times through our application. We're essentially testing with production data without altering our production environment.

This approach allows us to measure the effect of our adjustments and provides a quick feedback cycle. As a result:

- We don't have to guess whether our changes work and don't have unintended side effects that might cause invoices that previously worked to suddenly stop working.
- We don't have to wait until another "summary invoice" comes through production to see if our adjustments work (with the risk that the adjustment isn't sufficient, and we'd have to wait again before we can make another attempt).

In summary: We can shorten the feedback loop and quantify that our adjustments work.

Here's how it works: in Aigentic platform, we can tag runs that went well, which can be used for validation. We also tag the run with the edge case and indicate what output we actually expect. Now we can make the adjustment and immediately test in an isolated environment whether the output meets our expectations.

![Aigentic Platform](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880d31a2cbbb82447ea_AD_4nXeBSuLHSg3cvZauFwhxTMIrqTE-mXoPr32i1m5R2MJb3ICm7AJJdwIG2QCTbInVXNdJKYqZW20CRsGTqvGN828A7dkTcL4ao0djA7fb8ylc2kQR-A2vfI2CdmwsoayGsZ66YJpWaIwyPuw6XdH0bO2iFDQ0.png)

To indicate which arguments we actually expect in the tool call, the recorded tool call can be copied from Aigentic platform to the test:

![Tool Call](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880c68bb5bf027e78a3_AD_4nXcTw7oRQY0IblSu8jQEkJZQf5lb_TMFVBlp-NlvvMRvaAuOzJc1stxw3YSe8ILFNjqJF0U7BD-Ek_4Nv9KHRCuxRbjU4gPOoOET9EfP-8IAqmebJnh2dppeDPL4LgPBcUgM1t-cI7g18c6ZQmUPmcXz13K9.png)

Then we paste this into the regression test DSL and remove the invoice lines (the summary lines) that we don't expect ("Smart City Platform Implementation" and "Renewable Energy Integration Services"):

![Regression Test](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/676578804fea0e0cc2733018_AD_4nXdQc7oqQkIBmSOwQsyNAmkEiPaMTdlZWGTJJg9NfNhv8WRePi3K1hK255j8hCaxzlYWFcFhyk3dwooxy35fFzDUJmGK2n0hZldE-1aSbIDDj7VtaUdYXQBsRoZllg9eOIoCPQErEFHgqO1lR0S9bh_ed8zv.png)

‍

We've specified that the test should repeat for 5 iterations, allowing us to directly validate that the invoice details are consistently recognized. This is useful because LLMs aren't 100% deterministic by nature. 

Now before we add the extra "If the line items are summarized, please provide only the details of the invoice items" instruction the test should fail:

![Test Failure](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880726faf5bc8f55e82_AD_4nXcQ-RaPnNgS01h75iBsK0RCrpJDMbG05s9JZ5we8NU-DqYMJ-0Xb_G2E3A2gmSY28trxuVGQ9vdtVPqcvKiIAEO6DzacFOCLy4fnj6fClS83hA4pbCvcHg8it_XyGYWQhYq9ux8YF5cB0N102G9My_z-I4.png)

The test fails as expected. Note that it fails because of the 2 extra invoiceItems, the order of properties is also different but this doesn't affect the outcome. Now the test is setup correctly and we're able to reproduce the problem let's add the extra instruction to the agent:

![Agent Instruction](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880d65e7c9a1cfd14f5_AD_4nXdTKcu2xsWq_ax7NoZzxlVJZqNKSPE1oLdjJoHC8Jbo3Q4ZKOM3tFmQ3nta0H20QdLCxdFbwX4K3oeWBuICKW2IJHKlcEx0oUbqFL_26AzYTS5aB2nxDbV6cZ34wglYx_HRghvlzroVLrzziT09rUI7HLo.png)

After running the test again we now see that the LLM succeeds in recognizing the expected invoice items, and it's consistent too:

![Test Success](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880d8b2501514794583_AD_4nXcOaZxH9TjCtxHY7oAi_QTGyd5unQIBMY2oN-5KXh4KxMRzpo-JWGPfR9wDkmpRPDlR3pLefV4nC0je3N9DC857ORoH1N1UuJnRRYkmz-3CMAJXz9UPlHN2NRC_XbA-dfA7FcEHYVIsW6v9bLxjjFgky7s.png)

We now see that the adjustment works as intended. Next, we want to know if our adjustment hasn't led to invoices that were previously processed correctly suddenly producing errors. For this, the regressionTest DSL can be used again and we can test with the runs we've approved and tagged as "validated" to see if they're still executed in the same way. Aigentic compares whether the test run results match with the previously recorded production run. Since these were already correct, it's not necessary to override these "expectations" as was the case with the correction.

![Regression Test](https://cdn.prod.website-files.com/66c6e5b1832658d31dff397c/67657880b43cdebed4a5c13c_AD_4nXeZ_8QhqwGvyEbZ30HrOsGZvNS5mFbAClXADcY8fHfblD4uA0eepb0pRh-d81sTpziZbeKLTACWigLl0YLJjpBE8CSg18a_XEL9tL20uOpyTSSD3SF-0XX33ZD6FvwbRomGacOqI2AXGEsY8LuF8bdaTUw.png)

As can be seen in the test result, the extra instruction to ignore "summary lines" hasn't produced any undesired side effects. Each run has been successfully tested 5 times here as well. Invoices that were previously processed correctly are still processed in exactly the same way with the adjustment.

‍

With this, we've validated that:

- The adjustment works as expected; summary lines are no longer extracted.
- No regression has occurred; historical production runs are still processed in the same way.
- The agent delivers consistent results; for each invoice, 5x the same input yields 5x the same output.

## Conclusion

Throughout this post, we've explored the challenges that arise when moving LLM applications from proof of concept to production. As demonstrated by our invoice processing example, issues like edge cases and inconsistent outputs can quickly complicate what seemed like a straightforward project.

We've seen how a platform like Aigentic can help address these challenges by:

1. Facilitating the identification and isolation of edge cases
2. Enabling adjustments in a controlled environment
3. Supporting validation against both new and historical data
4. Ensuring consistency across multiple runs

The ability to shorten feedback loops and quantify the impact of adjustments can significantly streamline the development process for LLM applications. This approach allows teams to tackle real-world challenges more effectively, leading to faster development cycles and more reliable outcomes.

As AI and LLM technologies continue to evolve, tools that bridge the gap between concept and production will likely play an increasingly important role in the development process. By addressing the practical challenges of LLM application development.

For those interested in exploring this approach further, more information about Aigentic is available at [https://aigentic.io](https://aigentic.io).
