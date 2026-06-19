# SoClean — Prohibited Claims

All claims listed here are **banned from all marketing copy** unless supported by verified third-party laboratory testing, regulatory certification, or medical evidence on file.

Violating these rules creates legal and regulatory risk. The AI content system must flag or reject any output containing these terms.

---

## Absolute Dust Claims — PROHIBITED

| Prohibited phrase | Why prohibited | Safe alternative |
|---|---|---|
| ไร้ฝุ่น 100% | Absolute claim; no lab verification | ฝุ่นน้อย |
| dust-free | Same as above (English) | low-dust |
| zero dust | Absolute; unverifiable | reduced dust |
| ไม่มีฝุ่นเลย | Absolute claim | ฝุ่นน้อยกว่า |
| ปลอดฝุ่นสมบูรณ์แบบ | Absolute; no evidence | ฝุ่นน้อย เหมาะสำหรับออฟฟิศ |

**Rule:** "ฝุ่นน้อย" is approved. "ไร้ฝุ่น" used as an absolute is not.

---

## Medical and Safety Claims — PROHIBITED

| Prohibited phrase | Why prohibited | Safe alternative |
|---|---|---|
| antibacterial | Requires regulatory certification | ให้สัมผัสสะอาด |
| ฆ่าเชื้อโรค | Medical claim; no certification | — (do not use) |
| medically safe | Requires clinical evidence | เหมาะกับการใช้งานทุกวัน |
| ปลอดภัยทางการแพทย์ | Medical claim | — |
| hypoallergenic | Requires allergy testing certification | เนียนนุ่ม ไม่刺激ผิว (only if verified) |
| hypoallergenic (สำหรับผู้แพ้ง่าย) | Medical claim without proof | — |
| dermatologist tested | Requires clinical study | — |
| dermatologist approved | Requires clinical study | — |
| ทดสอบโดยแพทย์ | Requires clinical study | — |

---

## Superlative Claims — PROHIBITED (without proof)

| Prohibited phrase | Why prohibited | Safe alternative |
|---|---|---|
| สะอาดที่สุด | Superlative; unverified | ให้สัมผัสสะอาด |
| นุ่มที่สุด | Superlative; unverified | เนียนนุ่ม |
| ปลอดภัยที่สุด | Superlative; unverified | เหมาะกับการใช้งานทุกวัน |
| ดีที่สุด | Superlative; unverified | ดีสำหรับคุณ / คุ้มค่า |
| safest | English superlative; unverified | suitable for everyday use |
| cleanest | English superlative; unverified | gives a clean feel |
| softest | English superlative; unverified | soft and smooth |
| best tissue in Thailand | Market ranking claim; unverified | — |
| อันดับ 1 | Ranking claim; unverified unless sourced | — |
| ขายดีที่สุด | Sales rank claim; unverified | ลูกค้าซื้อซ้ำ (if data-supported) |

---

## Environmental / Certification Claims — PROHIBITED (without certification)

| Prohibited phrase | Why prohibited |
|---|---|
| eco-friendly / รักษ์สิ่งแวดล้อม | Requires verified certification |
| biodegradable / ย่อยสลายได้ | Requires test data |
| sustainably sourced | Requires supply chain audit |
| FSC certified | Requires FSC certification on file |
| carbon neutral | Requires carbon accounting audit |

---

## Competitor Comparison Claims — PROHIBITED

| Rule | Reason |
|---|---|
| Do not name any specific competitor brand | Legal and ethical risk |
| Do not claim "better than [competitor]" | Comparative advertising requires substantiation |
| Do not use competitor brand names in hashtags | Risk of confusion/infringement |

---

## Emotional / Fear Tactics — PROHIBITED

| Prohibited approach | Why prohibited |
|---|---|
| "ทิชชู่ทั่วไปทำให้คุณป่วย" | Unsubstantiated health scare |
| "ฝุ่นทิชชู่เป็นอันตราย" | Fear claim without verified data |
| Implying competitor products are unsafe | Comparative safety claim without evidence |

---

## AI Content System Enforcement Rules

When Agento generates content, the compliance checker must:

1. **Flag** any output containing words from this list.
2. **Block** publishing of flagged content until human review approves.
3. **Suggest** an approved alternative from `approved-claims.md`.
4. **Log** each flag event for compliance audit trail.

Prohibited word detection patterns (for AI prompt injection and output scanning):

```
PROHIBITED_TERMS_TH = [
  "ไร้ฝุ่น 100%", "ไม่มีฝุ่นเลย", "ปลอดฝุ่นสมบูรณ์แบบ",
  "antibacterial", "ฆ่าเชื้อโรค", "medically safe", "ปลอดภัยทางการแพทย์",
  "hypoallergenic", "dermatologist", "ทดสอบโดยแพทย์",
  "สะอาดที่สุด", "นุ่มที่สุด", "ปลอดภัยที่สุด", "ดีที่สุด",
  "อันดับ 1", "ขายดีที่สุด"
]

PROHIBITED_TERMS_EN = [
  "dust-free", "zero dust", "antibacterial",
  "medically safe", "hypoallergenic", "dermatologist",
  "safest", "cleanest", "softest", "best tissue",
  "100% dust"
]
```
