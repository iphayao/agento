# SoClean Brand Kit

Brand documentation for **SoClean** by BN Paper — the primary brand in the Agento content system.

## Quick Reference

| Field | Value |
|---|---|
| Brand | SoClean |
| Company | BN Paper |
| Category | Facial tissue |
| Positioning | สะอาด เนียนนุ่ม ฝุ่นน้อย |
| Product | 2-ply, 180 sheets, Pack of 5, Carton of 50 |
| Channels | TikTok Shop, Shopee, Lazada, Facebook, Resellers |

## Safe Claim Summary

| Use | Avoid |
|---|---|
| ฝุ่นน้อย | ไร้ฝุ่น 100% / dust-free |
| เนียนนุ่ม | นุ่มที่สุด / softest |
| ให้สัมผัสสะอาด | antibacterial / ฆ่าเชื้อโรค |
| คุ้มค่า | ถูกที่สุด / cheapest |
| เหมาะกับการใช้งานทุกวัน | ปลอดภัยที่สุด / safest |
| ลูกค้าบอกว่า... | สะอาดที่สุด / cleanest |

## Files in This Package

| File | Contents |
|---|---|
| `brand-profile.md` | Brand identity, product specs, values, competitive frame |
| `tone-of-voice.md` | Voice personality, Thai/English copy rules, do/don't examples |
| `messaging-framework.md` | Core messages by audience, channel matrix, proof points |
| `approved-claims.md` | Safe claim phrases with basis and usage rules |
| `prohibited-claims.md` | Banned phrases with rationale and safe alternatives |
| `customer-personas.md` | Four buyer personas with insights and key messages |
| `content-pillars.md` | Seven content pillars with claim guidance and frequency |
| `channel-strategy.md` | Per-channel format, tone, and content type rules |
| `compliance-guidelines.md` | Claim categories, AI review workflow, audit trail |
| `example-content.md` | Reference copy by pillar and channel |

## How to Use This Brand Kit

### For AI Content Generation (agento-worker)
Load relevant brand kit files into the system prompt at content generation time:

```python
# Minimal context for caption generation
context_files = [
    "approved-claims.md",
    "prohibited-claims.md",
    "tone-of-voice.md",
]

# Full context for campaign content
context_files = [
    "brand-profile.md",
    "tone-of-voice.md",
    "messaging-framework.md",
    "approved-claims.md",
    "prohibited-claims.md",
    "customer-personas.md",
    "content-pillars.md",
    "channel-strategy.md",
    "example-content.md",
]
```

### For Compliance Checking (agento-worker / agento-api)
Load `prohibited-claims.md` and extract term lists for automated scanning. See the `PROHIBITED_TERMS_TH` and `PROHIBITED_TERMS_EN` arrays in that file.

### For Human Editorial Review
Reference `approved-claims.md` to verify that claims are in scope. Reference `compliance-guidelines.md` for the approval workflow.

### For Campaign Planning
Reference `content-pillars.md` for the weekly content calendar. Use `channel-strategy.md` to adapt content per platform.

## Content Pillars Summary

1. **เนียนนุ่ม** — Softness; sensory and customer-review content
2. **สะอาด** — Clean feel; lifestyle and daily use content
3. **ฝุ่นน้อย** — Low dust; demo and office use content
4. **คุ้มค่า** — Value; pack math and carton pricing content
5. **เหมาะทุกที่** — Versatile use; home, office, shop content
6. **รีวิวจากลูกค้าจริง** — Social proof; review reposts
7. **ซื้อยกลัง** — Reseller content; carton pricing and B2B

## Target Personas Summary

1. **มิ้น (Gen Y)** — Primary household buyer; TikTok + Shopee
2. **คุณแม่บ้าน** — Family manager; Lazada + Facebook
3. **พี่โอ๋ (Office)** — Office supply buyer; Lazada + direct
4. **เจ้ของร้าน (Reseller)** — Carton buyer; LINE + direct

## Agento Integration Notes

- This package is consumed by `agento-worker` at content generation time.
- `agento-api` may reference prohibited terms for server-side validation.
- All generated content must pass automated compliance scan before human review.
- Content stays in DRAFT status until approved by a human editor.
- Future: brand kit files may be stored in the database and editable via the Agento web UI.
