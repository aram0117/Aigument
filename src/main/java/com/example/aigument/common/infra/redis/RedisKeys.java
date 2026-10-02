package com.example.aigument.common.infra.redis;

import com.example.aigument.common.infra.redis.enums.RedisPrefix;

/**
 * Redis 키 스키마 (v2/refactor-base)
 * <p>
 * <table>
 *   <tr><th>용도</th><th>키 패턴</th><th>자료구조</th><th>TTL</th></tr>
 *   <tr><td>채팅 메시지 로그 (AI 분석용)</td><td>{@code chat:room:log:{roomId}}</td><td>List</td><td>1시간</td></tr>
 *   <tr><td>채팅 분기 요약 로그 (AI 분석용)</td><td>{@code chat:room:summary:{roomId}}</td><td>String</td><td>1시간</td></tr>
 *   <tr><td>채팅 실시간 Pub/Sub</td><td>{@code chat:room:topic:{roomId}}</td><td>RTopic</td><td>없음</td></tr>
 *   <tr><td>채팅방 패턴 구독</td><td>{@code chat:room:*}</td><td>RPatternTopic</td><td>없음</td></tr>
 *   <tr><td>SMS 인증 코드</td><td>{@code sms:auth:{phone}}</td><td>String</td><td>5분</td></tr>
 *   <tr><td>로그아웃 토큰 블랙리스트</td><td>{@code (JWT raw token)}</td><td>String</td><td>토큰 만료까지</td></tr>
 * </table>
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    public static String chatRoomLog(Long chatRoomId) {
        return RedisPrefix.CHATROOM_LOG.getPrefix() + chatRoomId;
    }

    public static String chatSummaryLog(Long chatRoomId) {
        return RedisPrefix.CHATROOM_SUMMARY_LOG.getPrefix() + chatRoomId;
    }

    public static String chatRoomTopic(Long chatRoomId) {
        return RedisPrefix.CHATROOM_TOPIC.getPrefix() + chatRoomId;
    }

    public static String chatRoomPattern() {
        return RedisPrefix.CHATROOM_PATTERN.getPrefix();
    }

    public static String smsAuth(String phoneNumber) {
        return RedisPrefix.SMS_AUTH.getPrefix() + phoneNumber;
    }
}
