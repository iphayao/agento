-- Seed default SoClean brand profile (skipped if a brand profile already exists)
INSERT INTO brand_profiles (brand_name, slogan, tone_of_voice, target_audience, key_messages, prohibited_claims)
SELECT
    'SoClean',
    'สะอาด เนียนนุ่ม ไร้ฝุ่น',
    'เป็นกันเอง อบอุ่น มั่นใจ — สื่อสารภาษาเข้าใจง่าย ไม่เป็นทางการเกินไป เน้นความสะอาดและความอ่อนโยน',
    'ผู้หญิง Gen Y ที่มีกำลังซื้อ, ครัวเรือน, ผู้ซื้อสำหรับออฟฟิศ, ร้านค้าและตัวแทนจำหน่ายที่ซื้อยกลัง',
    'เนียนนุ่ม, ให้สัมผัสสะอาด, ฝุ่นน้อย, เหมาะกับการใช้งานทุกวัน, คุ้มค่าสำหรับบ้าน ร้านค้า และออฟฟิศ, ส่งเร็ว',
    '100% dust-free, medically safe, antibacterial, hypoallergenic, safest, cleanest, สะอาดที่สุด, ปลอดภัยที่สุด, ดีที่สุด, ไร้ฝุ่น 100%'
WHERE NOT EXISTS (SELECT 1 FROM brand_profiles);

-- Seed SoClean Facial Tissue product fact (skipped if any product already exists)
INSERT INTO product_facts (product_name, sku, sheet_count, ply, pack_size, carton_size, key_benefits, proof_points)
SELECT
    'SoClean Facial Tissue',
    'SCL-180-2PLY-5PK',
    180,
    2,
    5,
    50,
    'เนียนนุ่ม, ให้สัมผัสสะอาด, ฝุ่นน้อย, เหมาะกับการใช้งานทุกวัน',
    '2 ชั้น (2-ply), 180 แผ่นต่อห่อ, แพ็ค 5 ห่อ, ลัง 50 แพ็ค, เหมาะสำหรับบ้าน ออฟฟิศ และร้านค้า'
WHERE NOT EXISTS (SELECT 1 FROM product_facts);
