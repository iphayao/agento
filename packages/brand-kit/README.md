# packages/brand-kit

Brand voice, positioning, claim rules, and channel strategy for Agento.

Shared across the system:
- `agento-worker` loads files at content generation time to ground prompts in brand context
- `agento-api` references prohibited claim terms for server-side validation
- Human editors use this as the source of truth for all content decisions

## Structure

```
brand-kit/
  soclean/
    README.md                  # Quick reference + integration notes
    brand-profile.md           # Brand identity, product specs, values
    tone-of-voice.md           # Copy voice, Thai/English style rules
    messaging-framework.md     # Core messages by audience and channel
    approved-claims.md         # Safe claim phrases with usage guidance
    prohibited-claims.md       # Banned phrases with safe alternatives
    customer-personas.md       # Four buyer personas with key messages
    content-pillars.md         # Seven content pillars with claim rules
    channel-strategy.md        # Per-channel format and content strategy
    compliance-guidelines.md   # AI review workflow and audit rules
    example-content.md         # Reference copy by pillar and channel
```

## SoClean Quick Reference

**Product:** Facial tissue, 2-ply, 180 sheets, pack of 5, carton of 50
**Thai positioning:** สะอาด เนียนนุ่ม ฝุ่นน้อย
**Target:** Women Gen Y, households, office buyers, resellers

### Claim Safety

| Use | Avoid |
|---|---|
| ฝุ่นน้อย | ไร้ฝุ่น 100% / dust-free |
| ให้สัมผัสสะอาด | antibacterial / สะอาดที่สุด |
| เนียนนุ่ม | hypoallergenic / medically safe |
| เหมาะกับการใช้งานทุกวัน | ปลอดภัยที่สุด / safest |
| คุ้มค่าสำหรับบ้าน ร้านค้า และออฟฟิศ | ถูกที่สุด / cheapest |

### Channels
TikTok Shop | Shopee | Lazada | Facebook | Reseller / LINE

### Content Pillars
1. เนียนนุ่ม
2. สะอาด
3. ฝุ่นน้อย
4. คุ้มค่า
5. เหมาะทุกที่
6. รีวิวจากลูกค้าจริง
7. ซื้อยกลัง

## Adding a New Brand

To add a second brand, create a parallel directory:

```
brand-kit/
  soclean/     ← existing
  [brand2]/    ← new brand follows same file structure
    README.md
    brand-profile.md
    ...
```
