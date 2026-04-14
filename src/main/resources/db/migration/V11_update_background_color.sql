-- 2026/04/14 (화)
-- 배경색(hex_code) 컬럼의 데이터 타입을 VARCHAR(10)으로 변경, nullable로 변경
-- 클라 측에서 bg_colors를 관리하고 있기 때문에 백엔드에서는 id 존재 여부만 확인하는 측이 낫다고 판단함. 하지만 우선은 hex code값 넣어두기
ALTER TABLE bg_colors
    MODIFY COLUMN hex_code VARCHAR(10) NULL;

-- 기존에 있던 id 16 ~ 24는 삭제 완료 (운영서버에서는 이미 삭제되어 있음)
-- DELETE FROM bg_colors
-- WHERE id BETWEEN 16 AND 24;

UPDATE bg_colors
SET
    hex_code = CASE id
                   WHEN 1 THEN '0xFFF4B3BE'
                   WHEN 2 THEN '0xFFFCE29F'
                   WHEN 3 THEN '0xFFD8E6A8'
                   WHEN 4 THEN '0xFFCDF5FF'
                   WHEN 5 THEN '0xFFFAF8F4'
                   WHEN 6 THEN '0xFFF3CAD8'
                   WHEN 7 THEN '0xFFF8D495'
                   WHEN 8 THEN '0xFFC2DCAF'
                   WHEN 9 THEN '0xFFCDE8FF'
                   WHEN 10 THEN '0xFFDEDEDE'
                   WHEN 11 THEN '0xFFDCCDFC'
                   WHEN 12 THEN '0xFFF2C49B'
                   WHEN 13 THEN '0xFF9BC289'
                   WHEN 14 THEN '0xFFB8CCFF'
                   WHEN 15 THEN '0xFFB2B2B2'
        END,
    modified_at = NOW(6)
WHERE id BETWEEN 1 AND 15;
