-- =========================================================
-- V2: 블록 팔레트 마스터 데이터 52건 최초 삽입
-- GET /api/blocks 자유 제작 모드 응답용
-- 이미 적용된 마이그레이션은 수정하지 않고, 이후 변경은 새 버전에서 처리합니다.
-- =========================================================

INSERT INTO blocks (
    block_id,
    block_type,
    name,
    description,
    input_schema,
    option_schema,
    output_schema,
    default_execution_mode,
    status,
    created_at,
    updated_at
)
VALUES
    -- INPUT
    (
        1,
        'INPUT',
        '사용자 요청 받기',
        '사용자의 요청 문장을 받아 흐름의 출발점으로 사용합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        2,
        'INPUT',
        '목표 정하기',
        '이번 작업의 목표 유형을 정하고 완료 기준을 지정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        3,
        'INPUT',
        '주제 입력하기',
        '대표 주제와 키워드를 입력해 작업 범위를 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        4,
        'INPUT',
        '파일 업로드 받기',
        '문서·이미지를 드래그해 업로드합니다. 카드를 끌어 참고 우선순위를 정하고, 파일별 용도를 지정하세요.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        5,
        'INPUT',
        '필요한 문서 확인하기',
        '실행 전 필요한 자료 유형과 없을 때의 처리 방식을 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        6,
        'INPUT',
        '필요한 스킬 확인하기',
        '작업 유형을 복수로 고른 뒤 대표 스킬 1개를 지정합니다. 수행 순서는 사운드 블록처럼 끌어 정렬합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        7,
        'INPUT',
        '대상 독자 정하기',
        '결과물을 읽을 대상과 이해 수준을 지정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        8,
        'INPUT',
        '결과 사용 상황 정하기',
        '결과물이 쓰일 상황과 매체를 지정해 형식을 맞춥니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        9,
        'INPUT',
        '제약조건 입력하기',
        '분량·문체·포함/제외 규칙 등 결과물의 제약을 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),

    -- CONTEXT
    (
        10,
        'CONTEXT',
        '프로젝트 문서 불러오기',
        '프로젝트를 선택하고 참고할 문서를 지정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        11,
        'CONTEXT',
        '업로드 문서 읽기',
        '업로드한 파일의 읽기 범위를 지정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        12,
        'CONTEXT',
        '직접 입력 내용 사용하기',
        '직접 입력한 내용을 참고 자료로 사용합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        13,
        'CONTEXT',
        '참고 범위 정하기',
        '참고할 자료의 범위 방식을 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        14,
        'CONTEXT',
        '역할 부여하기',
        'AI가 수행할 역할과 관점을 지정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        15,
        'CONTEXT',
        '배경 설명 추가하기',
        '작업의 배경 상황과 현재 단계를 알려줍니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        16,
        'CONTEXT',
        '참고하지 말아야 할 내용 정하기',
        '참고에서 제외할 유형과 대상을 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),

    -- PROCESS
    (
        17,
        'PROCESS',
        '핵심 내용 추출하기',
        '자료에서 추출할 대상과 강도를 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        18,
        'PROCESS',
        '요약하기',
        '요약의 길이·형식·관점을 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        19,
        'PROCESS',
        '항목별로 분류하기',
        '분류 기준을 정하고 카테고리를 구성합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        20,
        'PROCESS',
        '비교하기',
        '비교 대상과 기준을 각각 카드로 만들어 정렬합니다. 기준마다 가중치 슬라이더로 중요도를 조절할 수 있습니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        21,
        'PROCESS',
        '순서대로 정리하기',
        '단계 카드를 끌어 순서를 지정합니다. 드래그 중 원래 위치에는 반투명 placeholder가, 놓을 위치에는 삽입선이 표시됩니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        22,
        'PROCESS',
        '기능으로 분해하기',
        '대상을 기능 단위로 분해합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        23,
        'PROCESS',
        '정책과 연결하기',
        '기능·화면을 정책 문서와 연결합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        24,
        'PROCESS',
        '예외 케이스 찾기',
        '점검할 예외 유형과 범위를 정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        25,
        'PROCESS',
        '초안 작성하기',
        '문서 유형과 목차 구성 방식을 정해 초안을 만듭니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        26,
        'PROCESS',
        '표로 재구성하기',
        '열 블록을 가로로 끌어 순서를 바꾸고, 행 기준을 정하면 아래 미리보기가 실시간으로 갱신됩니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        27,
        'PROCESS',
        '체크리스트로 바꾸기',
        '내용을 점검 가능한 체크리스트로 변환합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        28,
        'PROCESS',
        '질문 리스트 만들기',
        '확인이 필요한 질문 목록을 생성합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        29,
        'PROCESS',
        '특정 스킬 호출하기',
        '프리셋 스킬을 불러와 이전 결과에 적용합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        30,
        'PROCESS',
        '프롬프트 조립하기',
        '역할·작업·참고·조건·출력 조각을 끌어 순서대로 조립합니다. 아래 최종 프롬프트가 자동 생성됩니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        31,
        'PROCESS',
        '빈칸 프롬프트 채우기',
        '단어 카드를 문장의 빈칸으로 끌어다 놓습니다. 빈칸은 유형이 맞는 카드만 받아들입니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        32,
        'PROCESS',
        '요약 프롬프트 배치하기',
        '역할·작업·참고·출력 카드를 색과 아이콘으로 구분해 배치합니다. 각 카드에 한 줄 요약을 적고 상세 프롬프트를 연결할 수 있습니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),

    -- REVIEW
    (
        33,
        'REVIEW',
        '누락 확인하기',
        '빠진 항목이 없는지 기준에 따라 점검합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        34,
        'REVIEW',
        '형식 확인하기',
        '결과물이 기대 형식과 일치하는지 검사합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        35,
        'REVIEW',
        '조건 충족 확인하기',
        '이전 블록의 조건을 불러와 검사 순서를 정합니다. 각 조건은 소스 포트로 원본 블록과 연결됩니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        36,
        'REVIEW',
        '정책 충돌 확인하기',
        '기능과 정책, 정책 간 충돌을 점검합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        37,
        'REVIEW',
        '근거 확인하기',
        '주장·수치에 근거가 있는지 검사합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        38,
        'REVIEW',
        '중복 제거하기',
        '반복되는 내용을 정리합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        39,
        'REVIEW',
        '톤 조정하기',
        '결과물의 어조를 목표 톤에 맞게 다듬습니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        40,
        'REVIEW',
        '오류 위치 표시하기',
        '점검할 오류 유형과 표시 단위를 고릅니다. 캔버스 강조를 켜면 문제 블록이 캔버스에서 직접 표시됩니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        41,
        'REVIEW',
        '수정 가이드 제공하기',
        '검토 실패 시 어디를 어떻게 고칠지 안내합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),

    -- OUTPUT
    (
        42,
        'OUTPUT',
        '텍스트로 출력하기',
        '결과를 일반 텍스트 형태로 출력합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        43,
        'OUTPUT',
        '표로 출력하기',
        '출력할 표의 열을 카드로 만들어 순서를 정하고, 행 기준과 셀 분량을 지정합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        44,
        'OUTPUT',
        '체크리스트로 출력하기',
        '결과를 점검용 체크리스트로 출력합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        45,
        'OUTPUT',
        '문서 초안으로 출력하기',
        '문서 종류와 제목을 정하고, 직접 목차를 선택하면 섹션 블록을 끌어 순서를 편집합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        46,
        'OUTPUT',
        '발표용 요약으로 출력하기',
        '발표 시간에 맞는 요약을 출력합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        47,
        'OUTPUT',
        '개발자 전달용으로 출력하기',
        '개발 역할에 맞는 전달 문서를 출력합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        48,
        'OUTPUT',
        '프롬프트로 출력하기',
        '결과를 재사용 가능한 프롬프트로 출력합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        49,
        'OUTPUT',
        '단계별 가이드로 출력하기',
        '따라 할 수 있는 단계별 가이드로 출력합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        50,
        'OUTPUT',
        '내 저장소에 저장하기',
        '저장할 대상과 위치를 고릅니다. 흐름은 MVP 기준 비공개로 저장되며, 이후 공개로 전환할 수 있습니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        51,
        'OUTPUT',
        '공개용 설명 만들기',
        '흐름을 공개할 때 보일 소개 정보를 작성합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    ),
    (
        52,
        'OUTPUT',
        '복사 가능한 흐름으로 만들기',
        '다른 사용자가 복사해 쓸 수 있게 흐름을 정리합니다.',
        NULL, NULL, NULL,
        'PRESET',
        'ACTIVE',
        NOW(), NOW()
    );
