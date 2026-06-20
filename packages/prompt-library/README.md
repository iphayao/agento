# packages/prompt-library

Versioned LLM prompt templates for Agento content workflows.

Templates are self-contained YAML files with explicit input variables, structured JSON output schemas, claim safety rules, and SoClean-specific examples. The `agento-worker` Python service loads templates from this library to drive content generation.

---

## Directory Structure

```
prompt-library/
  README.md                     ← This file
  index.json                    ← Machine-readable registry of all templates
  schema/
    template-schema.json        ← JSON Schema defining the template file format
  shared/
    claim-safety.yaml           ← Canonical claim safety rules (prohibited + approved terms)
    brand-context.yaml          ← SoClean brand facts, personas, channels, tone guide
  templates/
    01-tiktok-video-script-v1.yaml
    02-tiktok-caption-v1.yaml
    03-shopee-product-description-v1.yaml
    04-lazada-product-description-v1.yaml
    05-facebook-post-v1.yaml
    06-reseller-sales-post-v1.yaml
    07-review-content-v1.yaml
    08-promotion-campaign-v1.yaml
    09-educational-content-v1.yaml
    10-comparison-content-v1.yaml
    11-softness-focused-v1.yaml
    12-cleanliness-focused-v1.yaml
    13-low-dust-focused-v1.yaml
    14-value-pack-focused-v1.yaml
    15-b2b-reseller-content-v1.yaml
    16-claim-compliance-review-v1.yaml
    17-content-editing-v1.yaml
    18-hook-generation-v1.yaml
    19-cta-generation-v1.yaml
    20-hashtag-generation-v1.yaml
```

---

## Template Index

| # | Name | Channel | Category | Claim Safety |
|---|------|---------|----------|-------------|
| 01 | TikTok Video Script | TikTok | generation | required |
| 02 | TikTok Caption | TikTok | generation | required |
| 03 | Shopee Product Description | Shopee | generation | required |
| 04 | Lazada Product Description | Lazada | generation | required |
| 05 | Facebook Post | Facebook | generation | required |
| 06 | Reseller Sales Post | Reseller | generation | required |
| 07 | Review-Based Content | Multi | generation | required |
| 08 | Promotion Campaign Content | Multi | generation | required |
| 09 | Educational Content | Multi | generation | required |
| 10 | Comparison Content | Multi | generation | required |
| 11 | Softness-Focused Content | Multi | generation | required |
| 12 | Cleanliness-Focused Content | Multi | generation | required |
| 13 | Low-Dust-Focused Content | Multi | generation | required |
| 14 | Value-Pack-Focused Content | Multi | generation | required |
| 15 | B2B Reseller Content | Reseller | generation | required |
| 16 | Claim Compliance Review | Any | compliance | required |
| 17 | Content Editing | Any | utility | required |
| 18 | Hook Generation | Multi | utility | required |
| 19 | CTA Generation | Multi | utility | — |
| 20 | Hashtag Generation | Multi | utility | required |

---

## Template Format

Each template is a YAML file with the following top-level fields:

```yaml
name: "Human-readable name"
slug: "machine-slug-v1"           # matches index.json
version: "1.0"
category: generation | utility | compliance
content_type: string
channel: tiktok | shopee | lazada | facebook | reseller | multi | any
brand: soclean
description: "One-line description"
purpose: >
  Detailed purpose and when to use.
claim_safety_required: true | false

variables:                         # Input variables with type and example
  - name: variable_name
    type: string | integer | boolean | array | enum
    required: true | false
    description: "..."
    example: "..."
    options: [...]                 # for enum types

system_prompt: |                   # Sent as the LLM's system message
  ...

user_prompt: |                     # Sent as the user message; uses {{variable_name}} placeholders
  ...

output_format: json
output_schema:                     # JSON Schema of the expected LLM response
  type: object
  properties: ...

example_input:                     # Example values for all variables
  variable_name: "value"

example_output:                    # Example JSON output using SoClean content
  field: "value"
```

The full schema is defined in `schema/template-schema.json`.

---

## Variable Substitution

Variables in `user_prompt` use double-brace syntax: `{{variable_name}}`.

In `agento-worker`, load the template, substitute variables, then call the LLM:

```python
import yaml
from pathlib import Path

def load_template(slug: str) -> dict:
    path = Path(f"packages/prompt-library/templates")
    for f in path.glob("*.yaml"):
        t = yaml.safe_load(f.read_text())
        if t["slug"] == slug:
            return t
    raise ValueError(f"Template not found: {slug}")

def build_prompt(template: dict, variables: dict) -> tuple[str, str]:
    system = template["system_prompt"]
    user = template["user_prompt"]
    for key, value in variables.items():
        placeholder = f"{{{{{key}}}}}"
        user = user.replace(placeholder, str(value))
    return system, user
```

---

## Output Handling

All templates output **structured JSON**. The `agento-worker` must:

1. Call the LLM with `response_format = { "type": "json_object" }` (if the provider supports it).
2. Parse the JSON response.
3. Validate against `output_schema` using `jsonschema`.
4. For content generation templates: check `compliance_check.is_safe`.
5. Save the result with `status = DRAFT`.
6. Never auto-publish — all content requires human editorial review.

```python
import json
import jsonschema

def handle_response(template: dict, raw_response: str) -> dict:
    output = json.loads(raw_response)
    jsonschema.validate(output, template["output_schema"])

    if template.get("claim_safety_required"):
        check = output.get("compliance_check", {})
        if not check.get("is_safe", False):
            flagged = check.get("prohibited_terms_detected", [])
            raise ComplianceViolationError(f"Prohibited terms detected: {flagged}")

    return output
```

---

## Compliance Gate

Every content generation template includes a `compliance_check` object in its output:

```json
{
  "compliance_check": {
    "approved_claims_used": ["ฝุ่นน้อย", "เนียนนุ่ม"],
    "prohibited_terms_detected": [],
    "is_safe": true
  }
}
```

The `16-claim-compliance-review-v1.yaml` template is the dedicated **compliance audit gate**. Run it on any content piece before presenting it to the human editor:

```
AI generates draft content
        ↓
Run template 16 (claim-compliance-review) on the output
        ↓
If BLOCKED → hold as DRAFT, surface flagged terms to editor
If REVIEW_NEEDED → hold as DRAFT, flag borderline terms
If SAFE → still DRAFT, present to human editor for approval
        ↓
Human editor reviews → APPROVED or REJECTED
```

---

## Claim Safety Summary

### Always Prohibited

| Thai | English | Use Instead |
|------|---------|-------------|
| ไร้ฝุ่น 100%, ไม่มีฝุ่นเลย | dust-free, zero dust | ฝุ่นน้อย / low-dust |
| antibacterial, ฆ่าเชื้อโรค | antibacterial | ให้สัมผัสสะอาด |
| medically safe, ปลอดภัยทางการแพทย์ | medically safe | เหมาะกับการใช้งานทุกวัน |
| hypoallergenic | hypoallergenic | เนียนนุ่ม |
| สะอาดที่สุด, นุ่มที่สุด, ปลอดภัยที่สุด | safest, cleanest, softest | approved relative claims |
| อันดับ 1, ขายดีที่สุด | #1, best tissue | — |

### Always Approved

- เนียนนุ่ม / soft and smooth
- ฝุ่นน้อย / low-dust
- ให้สัมผัสสะอาด / gives a clean feel
- เหมาะกับการใช้งานทุกวัน / suitable for everyday use
- คุ้มค่าสำหรับบ้าน ร้านค้า และออฟฟิศ
- Product specs: 2-ply, 180 sheets, 900 sheets/pack, 50 packs/carton

Full rules: `../brand-kit/soclean/prohibited-claims.md` and `approved-claims.md`.

---

## Adding a New Template

1. Copy an existing template YAML as a starting point.
2. Assign the next sequential ID and filename.
3. Set a unique `slug` in the format `<topic>-v1`.
4. Add the template entry to `index.json`.
5. Run `jsonschema` validation against `schema/template-schema.json`.
6. Test with a real SoClean content brief before committing.

---

## Versioning

Template versions follow `major.minor` in the `version` field:
- `1.0` — initial version
- `1.1` — minor improvement to prompts (backward-compatible output schema)
- `2.0` — breaking change to output schema

Update the filename slug when bumping the major version: `tiktok-video-script-v2.yaml`.
Keep `v1` file in place until all callers are migrated.

---

## Related Files

- `../brand-kit/soclean/` — Full brand kit (all content must comply)
- `../content-schemas/` — Pydantic/JSON schemas for content objects stored in the database
- `../../apps/agento-worker/` — Python FastAPI service that executes these templates
- `../../apps/agento-api/` — Spring Boot API that stores generated content as DRAFT
