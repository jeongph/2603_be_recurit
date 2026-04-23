-- V001: 의도적 no-op baseline.
-- 도메인 테이블(subscription, subscription_event)은 V002 이후 마이그레이션에서 추가한다.
-- Flyway 초기 상태를 수립하기 위한 anchor 이므로 삭제하지 말 것.
SELECT 1;
