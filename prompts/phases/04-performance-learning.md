# Prompt 04 — Performance Learning

```text
You are an expert full-stack engineer and marketing analytics system designer.

Read CLAUDE.md first.
Review the existing implementation before editing.

Implement only Phase 4 of Agento: Performance Learning.

Goal:
Add performance learning so Agento can learn which content angles, hooks, CTAs, and channels perform best.

New entities:
1. ContentPerformance
   - id
   - generatedContentId
   - channel
   - publishedAt
   - impressions
   - views
   - clicks
   - likes
   - comments
   - shares
   - orders
   - revenue
   - conversionRate
   - engagementRate
   - cost
   - roas
   - notes
   - createdAt
   - updatedAt

2. ContentInsight
   - id
   - generatedContentId
   - campaignId
   - insightType: WINNING_HOOK, WINNING_ANGLE, LOW_PERFORMING_ANGLE, STRONG_CTA, WEAK_CTA, AUDIENCE_SIGNAL, CHANNEL_SIGNAL
   - insightText
   - confidenceScore
   - createdAt

3. PerformanceSummary
   - id
   - periodStart
   - periodEnd
   - channel
   - summaryText
   - recommendedAngles
   - recommendedHooks
   - recommendedCTAs
   - avoidPatterns
   - createdAt

Backend requirements:
1. Add CRUD APIs for content performance.
2. Add CSV import API.
3. Add analytics service to calculate engagement rate, conversion rate, revenue per content, ROAS, top content, weak content.
4. Add insight generation API: POST /api/performance/analyze
5. Store generated insights.
6. Feed winning insights back into knowledge base as WINNING_CONTENT or MARKET_INSIGHT.

Agento-worker requirements:
1. Add Performance Analyst Agent.
2. Analyze best hooks, best angles, best CTAs, best channels, and poor-performing patterns.
3. Output structured insights.
4. Future content workflow should retrieve performance insights.

Frontend requirements:
1. Performance dashboard
2. Content performance table
3. CSV import screen
4. Top content ranking
5. Insight generation button
6. Recommended next content angles
7. Recommended hooks and CTAs

Acceptance criteria:
1. User can add performance metrics manually.
2. User can import CSV.
3. Dashboard shows best and worst content.
4. System generates insights.
5. Insights are stored in knowledge base.
6. Future generated content can use performance insights.

Do not implement direct platform API integrations yet.

Before editing:
1. Explain task goal.
2. List files to create or modify.
3. Confirm architecture rules.
4. Produce implementation plan.

After editing:
1. Summarize changes.
2. List tests added.
3. Explain CSV format.
4. Recommend next prompt.
```
