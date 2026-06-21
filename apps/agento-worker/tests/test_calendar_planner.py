"""Tests for the Calendar Planner agent."""

import json
from unittest.mock import AsyncMock, patch

import pytest

from app.calendar.models import CalendarItemSuggestion, CalendarPlanRequest
from app.calendar.planner import generate_plan
from app.models import BrandProfile, ProductFact


@pytest.fixture
def brand():
    return BrandProfile(
        brandName="SoClean",
        slogan="สะอาด เนียนนุ่ม ฝุ่นน้อย",
        toneOfVoice="friendly, trustworthy",
        targetAudience="Women Gen Y, households, office buyers",
        keyMessages=["ฝุ่นน้อย", "เนียนนุ่ม", "คุ้มค่า"],
        prohibitedClaims=["dust-free 100%", "antibacterial", "safest"],
    )


@pytest.fixture
def product():
    return ProductFact(
        productName="SoClean Facial Tissue",
        sku="SC-FT-180",
        ply=2,
        sheetCount=180,
        packSize=5,
        cartonSize=50,
        keyBenefits=["ฝุ่นน้อย", "เนียนนุ่ม", "ทนทาน"],
        proofPoints=["ผ่านการทดสอบ", "ผลิตจากเยื่อกระดาษคุณภาพสูง"],
    )


@pytest.fixture
def plan_request(brand, product):
    return CalendarPlanRequest(
        calendarId="cal-123",
        brand=brand,
        products=[product],
        periodStart="2026-06-01",
        periodEnd="2026-06-07",
        objective="Drive TikTok sales",
        numItems=3,
    )


@pytest.mark.asyncio
async def test_generate_plan_returns_suggestions(plan_request):
    mock_response = json.dumps({
        "suggestions": [
            {
                "plannedDate": "2026-06-01",
                "channel": "tiktok",
                "contentType": "TIKTOK_CAPTION",
                "contentAngle": "ทำไมต้องเลือก SoClean ฝุ่นน้อย เหมาะสำหรับออฟฟิศ",
                "targetAudience": "Women Gen Y ที่ทำงานออฟฟิศ",
                "hookDirection": "เปิดด้วยคำถาม: ทิชชู่ที่คุณใช้ทำให้มีฝุ่นไหม?",
                "ctaDirection": "ชวนให้กด Link ใน Bio เพื่อสั่งซื้อ",
            },
            {
                "plannedDate": "2026-06-03",
                "channel": "shopee",
                "contentType": "SHOPEE_DESCRIPTION",
                "contentAngle": "คุ้มค่าสำหรับบ้านและร้านค้า",
                "targetAudience": "ผู้ซื้อรายบ้านและผู้ค้าส่ง",
                "hookDirection": "ราคาดี ส่งไว จาก SoClean",
                "ctaDirection": "กดสั่งซื้อรับส่วนลดพิเศษ",
            },
            {
                "plannedDate": "2026-06-05",
                "channel": "facebook",
                "contentType": "FACEBOOK_POST",
                "contentAngle": "ใช้ทิชชู่ทุกวันต้องเลือกดี",
                "targetAudience": "แม่บ้าน Gen Y",
                "hookDirection": "รูปภาพ before/after การใช้งาน",
                "ctaDirection": "แสดงความคิดเห็นว่าชอบทิชชู่แบบไหน",
            },
        ]
    })

    with patch("app.calendar.planner._call_llm", new_callable=AsyncMock) as mock_llm:
        mock_llm.return_value = mock_response
        suggestions = await generate_plan(plan_request)

    assert len(suggestions) == 3
    assert suggestions[0].channel == "tiktok"
    assert suggestions[0].plannedDate == "2026-06-01"
    assert suggestions[1].channel == "shopee"
    assert suggestions[2].channel == "facebook"


@pytest.mark.asyncio
async def test_generate_plan_skips_invalid_suggestions(plan_request):
    mock_response = json.dumps({
        "suggestions": [
            {
                "plannedDate": "2026-06-01",
                "channel": "tiktok",
                "contentType": "TIKTOK_CAPTION",
                "contentAngle": "valid angle",
                "targetAudience": "Women Gen Y",
                "hookDirection": "hook",
                "ctaDirection": "cta",
            },
            "not a dict",
            None,
        ]
    })

    with patch("app.calendar.planner._call_llm", new_callable=AsyncMock) as mock_llm:
        mock_llm.return_value = mock_response
        suggestions = await generate_plan(plan_request)

    assert len(suggestions) == 1
    assert suggestions[0].channel == "tiktok"


@pytest.mark.asyncio
async def test_generate_plan_handles_empty_products(brand):
    request = CalendarPlanRequest(
        calendarId="cal-456",
        brand=brand,
        products=[],
        periodStart="2026-06-01",
        periodEnd="2026-06-03",
        objective="",
        numItems=2,
    )

    mock_response = json.dumps({
        "suggestions": [
            {
                "plannedDate": "2026-06-01",
                "channel": "tiktok",
                "contentType": "TIKTOK_SCRIPT",
                "contentAngle": "เนียนนุ่ม ใช้ดี",
                "targetAudience": "ผู้ใช้ทั่วไป",
                "hookDirection": "เริ่มด้วยการสาธิต",
                "ctaDirection": "กดดูรีวิว",
            },
        ]
    })

    with patch("app.calendar.planner._call_llm", new_callable=AsyncMock) as mock_llm:
        mock_llm.return_value = mock_response
        suggestions = await generate_plan(request)

    assert len(suggestions) == 1
    mock_llm.assert_called_once()


@pytest.mark.asyncio
async def test_generate_plan_returns_empty_on_empty_llm_suggestions(plan_request):
    mock_response = json.dumps({"suggestions": []})

    with patch("app.calendar.planner._call_llm", new_callable=AsyncMock) as mock_llm:
        mock_llm.return_value = mock_response
        suggestions = await generate_plan(plan_request)

    assert suggestions == []
