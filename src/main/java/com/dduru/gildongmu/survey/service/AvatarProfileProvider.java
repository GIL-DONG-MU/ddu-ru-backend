package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AvatarProfileProvider {

    private static final Map<AvatarType, AvatarProfile> PROFILES = Map.of(
            AvatarType.TTUR_POGUNI, new AvatarProfile(
                    "느긋하고 신중한 스타일. 계획은 최소한으로 세우되 자신의 리듬은 확실히 지킴.",
                    "조용한 명소 찾기, 가성비 좋은 맛집 발굴, 일정 중 여유 시간 확보.",
                    "일정에 자유시간을 조금만 넣어도 만족도가 올라가요."
            ),
            AvatarType.TTUR_MOOD, new AvatarProfile(
                    "카페, 전경, 감성 스팟을 선호하며 여행의 '분위기'를 가장 중요하게 생각함.",
                    "예쁜 코스 및 뷰 포인트 찾기, 사진 찍기 좋은 장소 안내.",
                    "기록에 집중하느라 현재를 놓치지 않도록 '5분 기록, 5분 즐기기' 규칙을 세워보세요."
            ),
            AvatarType.TTUR_MALLANGI, new AvatarProfile(
                    "여행 중 동행자의 분위기를 살피고 잘 맞춰주는 따뜻한 스타일. 검소한 여행 선호.",
                    "인원 조율 및 감정 관리, 합리적인 식당 및 코스 추천.",
                    "너무 배려만 하지 말고 '하루 한 번은 내 선택!' 규칙을 실천해 보세요."
            ),
            AvatarType.TTUR_SWEET, new AvatarProfile(
                    "사람을 편안하게 해주며, 맛있는 것과 좋은 분위기만 있다면 행복한 미식 힐링러.",
                    "단체 일정 코디, 실패 없는 맛집 픽, 여행 중 쉼표 만들어주기.",
                    "활동적인 친구와 여행할 때는 '식사 후 자유시간'으로 완급을 조절하세요."
            ),
            AvatarType.TTUR_POPO, new AvatarProfile(
                    "새로움을 추구하지만 위험은 피하는 실속파. 효율과 재미의 밸런스를 잘 잡음.",
                    "정보 탐색 및 길 찾기 능력 우수, 혼자서도 뛰어난 문제 해결 능력.",
                    "하루 한 번 정도는 예산을 신경 쓰지 않는 '플랙스 타임'을 가져보세요."
            ),
            AvatarType.TTUR_SPARKLE, new AvatarProfile(
                    "즉흥적으로 떠나는 것을 즐기며 비용보다 긍정적인 경험의 가치를 높게 평가함.",
                    "숨겨진 스팟 개척, 급격한 일정 변경에도 빠른 적응.",
                    "예산 관리가 어려울 수 있으니 알뜰한 친구와 일정을 미리 의논해 보세요."
            ),
            AvatarType.TTUR_GLIMMING, new AvatarProfile(
                    "새로운 자극과 인생샷에 진심인 미식 모험가. 즉흥적인 계획도 긍정적으로 수용.",
                    "현지인 추천 코스 실행력, 인생샷 스팟 캐치, 북적이는 에너지 유지.",
                    "일정이 사람 중심으로 흐를 수 있으니 하루 30분은 온전한 개인 시간을 가져보세요."
            ),
            AvatarType.TTUR_PADO, new AvatarProfile(
                    "어디서든 사람들과 잘 어울리며 분위기를 살리는 흥부자. 지출에 쿨한 인싸 유형.",
                    "현지 액티비티 빠른 예약, SNS 기록 담당, 높은 여행 에너지 유지.",
                    "체력과 예산 소모가 클 수 있으니 하루 1~2개의 핵심 활동에 집중해 보세요."
            )
    );

    public AvatarProfile getProfile(AvatarType avatarType) {
        return PROFILES.get(avatarType);
    }

    public record AvatarProfile(String personality, String strength, String tip) {
    }
}
