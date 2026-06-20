"""Tests for the prohibited-term compliance checker.

Verifies that find_prohibited_terms() mirrors the logic in ComplianceChecker.java:
- NFC Unicode normalization for Thai text
- Case-insensitive matching for English terms
- Clean alternatives pass without false positives
"""

import unicodedata

import pytest

from app.workflow.agents import find_prohibited_terms


def test_clean_text_returns_empty():
    assert find_prohibited_terms("ทิชชู่ SoClean เนียนนุ่ม ฝุ่นน้อย") == []


def test_none_returns_empty():
    assert find_prohibited_terms(None) == []


def test_empty_string_returns_empty():
    assert find_prohibited_terms("") == []


def test_detects_thai_superlative():
    found = find_prohibited_terms("ทิชชู่ที่ดีที่สุดในตลาด")
    assert "ดีที่สุด" in found


def test_detects_thai_cleanliness_claim():
    found = find_prohibited_terms("กระดาษสะอาดที่สุดในประเทศ")
    assert "สะอาดที่สุด" in found


def test_detects_english_dust_free():
    found = find_prohibited_terms("Our tissue is dust-free")
    assert "dust-free" in found


def test_detects_english_antibacterial():
    found = find_prohibited_terms("antibacterial tissue paper")
    assert "antibacterial" in found


def test_english_detection_is_case_insensitive():
    found = find_prohibited_terms("DUST-FREE and SAFEST choice")
    assert "dust-free" in found
    assert "safest" in found


def test_nfc_normalization_matches_nfd_encoded_text():
    # Same Thai characters with NFD (decomposed) encoding should still match
    nfd_text = unicodedata.normalize("NFD", "ดีที่สุด")
    found = find_prohibited_terms(f"ทิชชู่ {nfd_text}")
    assert "ดีที่สุด" in found


def test_multiple_english_violations_all_detected():
    text = "safest, cleanest, and dermatologist approved tissue"
    found = find_prohibited_terms(text)
    assert "safest" in found
    assert "cleanest" in found
    assert "dermatologist" in found


def test_approved_thai_alternatives_do_not_trigger():
    clean = "ทิชชู่ฝุ่นน้อย เนียนนุ่ม คุ้มค่า เหมาะกับการใช้งานทุกวัน ให้สัมผัสสะอาด"
    assert find_prohibited_terms(clean) == []


def test_approved_english_alternatives_do_not_trigger():
    clean = "low dust tissue, soft and smooth, great value for everyday use"
    assert find_prohibited_terms(clean) == []


def test_no_false_positive_on_brand_name():
    assert find_prohibited_terms("SoClean #SoClean #ทิชชู่SoClean") == []
