package com.example.aigument.common.aop;

/**
 * AOP 어드바이저 실행 우선순위.
 * <p>
 * {@code @Order} 값이 낮을수록 바깥(outer) 레이어에서 먼저 실행됩니다.
 * <pre>
 * 호출자 → [LatencyAop] → [@Transactional] → 실제 메서드
 * </pre>
 */
public final class AopOrder {

    private AopOrder() {
    }

    /**
     * 지연 시간 측정 — 트랜잭션 시작·커밋/롤백을 포함한 전체 실행 시간을 재기 위해 가장 바깥에 둡니다.
     */
    public static final int LATENCY = 1;

    /**
     * 트랜잭션 경계 — LatencyAop 내부에서 동작합니다.
     */
    public static final int TRANSACTION = 2;
}
