# packages/brand-kit

Brand voice, positioning, and claim rules for Agento — starting with SoClean.

This package is shared across the system:
- `agento-worker` loads it at content generation time to ground prompts in brand context
- `agento-api` may reference claim rules for server-side validation

## Contents (planned)

```
brand-kit/
  soclean/
    brand-profile.json     # Brand name, positioning, tone, target audience
    product-facts.json     # Product specifications and verified claims
    claim-rules.json       # Allowed and banned claim phrases
    tone-guide.md          # Copywriting voice and style guide (Thai + English)
    channel-specs.json     # Per-channel content format requirements
```

## SoClean Quick Reference

**Product:** Facial tissue, 2-ply, 180 sheets, pack of 5, carton of 50
**Thai positioning:** สะอาด เนียนนุ่ม ฝุ่นน้อย
**Target:** Women Gen Y, households, office buyers, resellers

### Safe claim language

| Use | Avoid |
|---|---|
| ฝุ่นน้อย | 100% ไร้ฝุ่น / dust-free |
| ให้สัมผัสสะอาด | antibacterial / สะอาดที่สุด |
| เนียนนุ่ม | hypoallergenic / medically safe |
| เหมาะกับการใช้งานทุกวัน | ปลอดภัยที่สุด |
| คุ้มค่าสำหรับบ้าน ร้านค้า และออฟฟิศ | safest / cleanest |

## TODO

- [ ] Create `soclean/brand-profile.json` (Phase 1)
- [ ] Create `soclean/claim-rules.json` (Phase 1)
- [ ] Create `soclean/tone-guide.md` (Phase 1)
- [ ] Create `soclean/channel-specs.json` (Phase 1)
