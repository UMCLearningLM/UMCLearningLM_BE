-- Seed detailed prompt metadata for the demo code-refactoring workflow.
INSERT INTO `prompt_templates` (
    `stage`, `name`, `template_key`, `prompt_body`, `version`,
    `is_active`, `created_at`, `updated_at`
)
VALUES
    ('INPUT', '입력값 수집 기본 템플릿', 'INPUT_COLLECT',
     '[INPUT 단계]\n수행 작업:\n{blockInstruction}\n\n사용자 입력:\n{input}\n\n입력 옵션:\n{options}', 'v1', TRUE, NOW(), NOW()),
    ('INPUT', '외부 리소스 입력 템플릿', 'INPUT_RESOURCE',
     '[INPUT 단계 - 외부 리소스]\n수행 작업:\n{blockInstruction}\n\n파일 및 리소스 정보:\n{input}\n\n처리 옵션:\n{options}\n\n파일의 실제 내용은 CONTEXT 단계에서 추출합니다.', 'v1', TRUE, NOW(), NOW()),
    ('INPUT', '기능 및 스킬 선택 템플릿', 'INPUT_CAPABILITY',
     '[INPUT 단계 - 기능 선택]\n수행 작업:\n{blockInstruction}\n\n필요 기능:\n{input}\n\n선택 옵션:\n{options}', 'v1', TRUE, NOW(), NOW()),
    ('CONTEXT', '컨텍스트 구성 템플릿', 'CONTEXT_RESOLVE',
     '[CONTEXT 단계]\n수행 작업:\n{blockInstruction}\n\n입력값:\n{input}\n\n처리 옵션:\n{options}\n\n확정된 컨텍스트:\n{resolvedContext}', 'v1', TRUE, NOW(), NOW()),
    ('CONTEXT', 'AI 역할 설정 템플릿', 'CONTEXT_ROLE',
     '[CONTEXT 단계 - 역할]\n수행 작업:\n{blockInstruction}\n\n역할 설정값:\n{input}\n\n역할 옵션:\n{options}', 'v1', TRUE, NOW(), NOW()),
    ('PROCESS', '분석 작업 템플릿', 'PROCESS_ANALYZE',
     '[PROCESS 단계 - 분석]\n수행 작업:\n{blockInstruction}\n\n분석 입력:\n{input}\n\n분석 옵션:\n{options}\n\n사용 가능한 자료:\n{resolvedContext}\n\n근거 없이 내용을 생성하지 말고 제공된 자료를 기준으로 분석합니다.', 'v1', TRUE, NOW(), NOW()),
    ('PROCESS', '변환 작업 템플릿', 'PROCESS_TRANSFORM',
     '[PROCESS 단계 - 변환]\n수행 작업:\n{blockInstruction}\n\n변환 대상:\n{input}\n\n변환 옵션:\n{options}\n\n참고 컨텍스트:\n{resolvedContext}\n\n기존 동작과 요구사항을 보존합니다.', 'v1', TRUE, NOW(), NOW()),
    ('PROCESS', '비교 결과 작성 템플릿', 'PROCESS_COMPARE',
     '[PROCESS 단계 - 비교]\n수행 작업:\n{blockInstruction}\n\n비교 대상:\n{input}\n\n비교 옵션:\n{options}\n\n각 변경의 이유와 기대 효과를 함께 작성합니다.', 'v1', TRUE, NOW(), NOW()),
    ('REVIEW', '결과 검증 템플릿', 'REVIEW_VALIDATE',
     '[REVIEW 단계 - 검증]\n수행 작업:\n{blockInstruction}\n\n검토 대상:\n{input}\n\n검토 기준:\n{options}\n\n발견한 문제와 통과 여부를 구분하여 반환합니다.', 'v1', TRUE, NOW(), NOW()),
    ('REVIEW', '결과 보완 템플릿', 'REVIEW_REFINE',
     '[REVIEW 단계 - 보완]\n수행 작업:\n{blockInstruction}\n\n보완 대상:\n{input}\n\n보완 옵션:\n{options}\n\n기존 의미는 유지하면서 전달력을 개선합니다.', 'v1', TRUE, NOW(), NOW()),
    ('OUTPUT', '개발 문서 출력 템플릿', 'OUTPUT_DOCUMENT',
     '[OUTPUT 단계 - 개발 문서]\n수행 작업:\n{blockInstruction}\n\n최종 결과:\n{input}\n\n출력 옵션:\n{options}\n\n개발자가 바로 검토할 수 있는 구조로 출력합니다.', 'v1', TRUE, NOW(), NOW()),
    ('OUTPUT', '체크리스트 출력 템플릿', 'OUTPUT_CHECKLIST',
     '[OUTPUT 단계 - 체크리스트]\n수행 작업:\n{blockInstruction}\n\n검토 결과:\n{input}\n\n출력 옵션:\n{options}\n\n각 항목을 실행 가능한 체크리스트로 변환합니다.', 'v1', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    `stage` = VALUES(`stage`),
    `name` = VALUES(`name`),
    `prompt_body` = VALUES(`prompt_body`),
    `is_active` = VALUES(`is_active`),
    `updated_at` = VALUES(`updated_at`);

UPDATE `blocks` SET
    `name` = '사용자 요청 받기',
    `description` = '사용자가 원하는 코드 분석 및 리팩토링 요청을 입력받습니다.',
    `input_schema` = '{"type":"object","required":["request"],"properties":{"request":{"type":"string","minLength":10,"maxLength":4000}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"language":{"type":"string","enum":["KO","EN"],"default":"KO"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["normalizedRequest"],"properties":{"normalizedRequest":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '사용자의 요청에서 분석 목적, 요구사항과 제한 조건을 명확하게 정리합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='INPUT_COLLECT' AND `version`='v1')
WHERE `block_key` = 'IN-001';

UPDATE `blocks` SET
    `name` = '주제 입력하기', `description` = '분석할 기술, 프로젝트 또는 리팩토링 주제를 입력받습니다.',
    `input_schema` = '{"type":"object","required":["topic"],"properties":{"topic":{"type":"string","minLength":2,"maxLength":300}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"scope":{"type":"string","maxLength":500}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["topic"],"properties":{"topic":{"type":"string"},"scope":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '분석할 기술과 코드 영역을 하나의 명확한 주제로 정리합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='INPUT_COLLECT' AND `version`='v1')
WHERE `block_key` = 'IN-003';

UPDATE `blocks` SET
    `name` = '파일 업로드 받기', `description` = '분석할 소스 코드와 프로젝트 문서를 업로드받습니다.',
    `input_schema` = '{"type":"object","required":["fileIds"],"properties":{"fileIds":{"type":"array","items":{"type":"string"},"minItems":1}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"extractionMode":{"type":"string","enum":["FULL","CODE_ONLY"],"default":"FULL"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["documentRefs"],"properties":{"documentRefs":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_instruction` = '업로드된 파일 식별자와 파일 메타데이터를 후속 문서 추출 단계에 전달합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='INPUT_RESOURCE' AND `version`='v1')
WHERE `block_key` = 'IN-004';

UPDATE `blocks` SET
    `name` = '필요한 스킬 확인하기', `description` = '코드 분석에 필요한 언어, 프레임워크 및 외부 기능을 확인합니다.',
    `input_schema` = '{"type":"object","required":["requestedSkills"],"properties":{"requestedSkills":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"allowFallback":{"type":"boolean","default":false}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["selectedSkills"],"properties":{"selectedSkills":{"type":"array","items":{"type":"string"}},"unavailableSkills":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_instruction` = '요청을 수행하는 데 필요한 분석 스킬과 외부 기능을 식별합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='INPUT_CAPABILITY' AND `version`='v1')
WHERE `block_key` = 'IN-006';

UPDATE `blocks` SET
    `name` = '결과 사용 상황 정하기', `description` = '리팩토링 결과의 대상 독자와 사용 목적을 설정합니다.',
    `input_schema` = '{"type":"object","required":["targetAudience","useCase"],"properties":{"targetAudience":{"type":"string","maxLength":200},"useCase":{"type":"string","maxLength":500}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"tone":{"type":"string","enum":["CONCISE","PROFESSIONAL","EDUCATIONAL"],"default":"PROFESSIONAL"},"detailLevel":{"type":"string","enum":["BRIEF","STANDARD","DETAILED"],"default":"DETAILED"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["audienceContext"],"properties":{"audienceContext":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '결과를 사용할 대상과 상황에 맞게 출력 관점을 정의합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='INPUT_COLLECT' AND `version`='v1')
WHERE `block_key` = 'IN-008';

UPDATE `blocks` SET
    `name` = '업로드 문서 읽기', `description` = '업로드된 소스 코드와 문서에서 분석 가능한 텍스트를 추출합니다.',
    `input_schema` = '{"type":"object","required":["documentRefs"],"properties":{"documentRefs":{"type":"array","items":{"type":"string"},"minItems":1}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"includeCode":{"type":"boolean","default":true}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["sourceData"],"properties":{"sourceData":{"type":"string"},"files":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_instruction` = '추출된 코드, 디렉터리 구조, 설정과 문서 내용을 분석 컨텍스트로 제공합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='CONTEXT_RESOLVE' AND `version`='v1')
WHERE `block_key` = 'CTX-002';

UPDATE `blocks` SET
    `name` = '역할 부여하기', `description` = 'AI가 수행할 개발자 역할과 전문 수준을 설정합니다.',
    `input_schema` = '{"type":"object","required":["role"],"properties":{"role":{"type":"string","maxLength":300}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"seniority":{"type":"string","enum":["JUNIOR","MID","SENIOR","STAFF"],"default":"SENIOR"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["roleInstruction"],"properties":{"roleInstruction":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '지정된 기술 스택에 숙련된 시니어 개발자이자 코드 리뷰어 역할을 적용합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='CONTEXT_ROLE' AND `version`='v1')
WHERE `block_key` = 'CTX-005';

UPDATE `blocks` SET
    `name` = '배경 설명 추가하기', `description` = '프로젝트 목적, 기술 스택과 제약 조건을 추가합니다.',
    `input_schema` = '{"type":"object","required":["background"],"properties":{"background":{"type":"string","maxLength":5000}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"constraints":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["backgroundContext"],"properties":{"backgroundContext":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '프로젝트 배경과 변경 시 반드시 지켜야 하는 제약 조건을 분석 컨텍스트에 반영합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='CONTEXT_RESOLVE' AND `version`='v1')
WHERE `block_key` = 'CTX-006';

UPDATE `blocks` SET
    `name` = '핵심 내용 추출하기', `description` = '코드와 문서에서 주요 구조와 요구사항을 추출합니다.',
    `input_schema` = '{"type":"object","required":["sourceData"],"properties":{"sourceData":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"focus":{"type":"string","maxLength":300},"maxItems":{"type":"integer","minimum":1,"maximum":50,"default":15}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["keyPoints"],"properties":{"keyPoints":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_instruction` = '프로젝트 구조, 핵심 기능, 기술 스택과 주요 제약 조건을 추출합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='PROCESS_ANALYZE' AND `version`='v1')
WHERE `block_key` = 'PR-001';

UPDATE `blocks` SET
    `name` = '기능으로 분해하기', `description` = '프로젝트 코드를 도메인과 기능 단위로 분해합니다.',
    `input_schema` = '{"type":"object","required":["content"],"properties":{"content":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"decompositionLevel":{"type":"string","enum":["FILE","CLASS","METHOD","DOMAIN"],"default":"CLASS"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["components"],"properties":{"components":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `prompt_instruction` = '코드를 책임, 도메인, 모듈과 기능 단위로 분해하고 각 구성요소의 역할을 설명합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='PROCESS_TRANSFORM' AND `version`='v1')
WHERE `block_key` = 'PR-006';

UPDATE `blocks` SET
    `name` = '최신 개발 트렌드 및 공식 권장사항과 연결하기', `description` = '사용 기술의 공식 문서와 최신 권장사항을 코드에 연결합니다.',
    `input_schema` = '{"type":"object","required":["technology"],"properties":{"technology":{"type":"string"},"currentVersion":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"recencyDays":{"type":"integer","minimum":1,"maximum":365,"default":180},"sourcePolicy":{"type":"string","enum":["OFFICIAL_ONLY"],"default":"OFFICIAL_ONLY"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["recommendations"],"properties":{"recommendations":{"type":"array","items":{"type":"object","required":["title","rationale","sourceUrl"],"properties":{"title":{"type":"string"},"rationale":{"type":"string"},"sourceUrl":{"type":"string","format":"uri"}}}}},"additionalProperties":false}',
    `prompt_instruction` = '외부에서 조회한 공식 문서와 최신 권장사항을 현재 코드에 연결하고 출처를 함께 제시합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='PROCESS_ANALYZE' AND `version`='v1')
WHERE `block_key` = 'PR-007';

UPDATE `blocks` SET
    `name` = '문제점 및 개선 포인트 찾기', `description` = '코드 품질, 성능, 보안 및 유지보수 문제를 찾습니다.',
    `input_schema` = '{"type":"object","required":["code"],"properties":{"code":{"type":"string"},"context":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"categories":{"type":"array","items":{"type":"string","enum":["BUG","PERFORMANCE","SECURITY","MAINTAINABILITY","STYLE"]}},"minSeverity":{"type":"string","enum":["LOW","MEDIUM","HIGH","CRITICAL"],"default":"LOW"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["issues"],"properties":{"issues":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `prompt_instruction` = '코드에서 구체적인 문제를 찾고 심각도, 영향과 개선 방향을 작성합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='PROCESS_ANALYZE' AND `version`='v1')
WHERE `block_key` = 'PR-008';

UPDATE `blocks` SET
    `name` = '리팩토링 초안 작성하기', `description` = '발견된 문제를 바탕으로 개선 코드 초안을 작성합니다.',
    `input_schema` = '{"type":"object","required":["code","issues"],"properties":{"code":{"type":"string"},"issues":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"scope":{"type":"string","enum":["MINIMAL","CLASS","MODULE"],"default":"MINIMAL"},"preserveBehavior":{"type":"boolean","default":true}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["refactorDrafts"],"properties":{"refactorDrafts":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `prompt_instruction` = '기존 동작을 유지하면서 문제를 해결하는 최소 범위의 리팩토링 코드를 작성합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='PROCESS_TRANSFORM' AND `version`='v1')
WHERE `block_key` = 'PR-009';

UPDATE `blocks` SET
    `name` = '기존 코드와 개선 코드를 비교표로 재구성하기', `description` = '기존 코드와 개선 코드의 차이와 효과를 비교표로 만듭니다.',
    `input_schema` = '{"type":"object","required":["originalCode","improvedCode"],"properties":{"originalCode":{"type":"string"},"improvedCode":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"columns":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["comparisons"],"properties":{"comparisons":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `prompt_instruction` = '기존 코드, 개선 코드, 변경 이유와 기대 효과를 행 단위 비교 자료로 구성합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='PROCESS_COMPARE' AND `version`='v1')
WHERE `block_key` = 'PR-010';

UPDATE `blocks` SET
    `name` = '누락 확인하기', `description` = '사용자 요구사항과 결과를 비교하여 누락된 내용을 확인합니다.',
    `input_schema` = '{"type":"object","required":["requirements","draft"],"properties":{"requirements":{"type":"string"},"draft":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"strictness":{"type":"string","enum":["NORMAL","STRICT"],"default":"STRICT"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["passed","missingItems"],"properties":{"passed":{"type":"boolean"},"missingItems":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_instruction` = '원래 요구사항과 작성된 결과를 대조하여 누락되거나 충분히 다뤄지지 않은 항목을 찾습니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='REVIEW_VALIDATE' AND `version`='v1')
WHERE `block_key` = 'RV-001';

UPDATE `blocks` SET
    `name` = '형식 확인하기', `description` = '최종 결과가 지정한 문서 형식을 준수하는지 확인합니다.',
    `input_schema` = '{"type":"object","required":["document"],"properties":{"document":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"requiredSections":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["passed","violations"],"properties":{"passed":{"type":"boolean"},"violations":{"type":"array","items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_instruction` = '필수 섹션, 코드 블록, 비교표와 체크리스트 형식이 올바른지 확인합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='REVIEW_VALIDATE' AND `version`='v1')
WHERE `block_key` = 'RV-002';

UPDATE `blocks` SET
    `name` = '개선이 필요한 코드 위치 표시하기', `description` = '문제가 발생한 파일과 코드 위치를 구체적으로 표시합니다.',
    `input_schema` = '{"type":"object","required":["issues"],"properties":{"issues":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"locationPrecision":{"type":"string","enum":["FILE","CLASS","METHOD","LINE"],"default":"LINE"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["annotations"],"properties":{"annotations":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `prompt_instruction` = '각 개선 사항에 파일 경로와 가능한 경우 클래스, 메서드 및 줄 위치를 연결합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='REVIEW_REFINE' AND `version`='v1')
WHERE `block_key` = 'RV-009';

UPDATE `blocks` SET
    `name` = '시니어 개발자 코드 리뷰 톤으로 조정하기', `description` = '분석 결과를 전문적인 코드 리뷰 문체로 조정합니다.',
    `input_schema` = '{"type":"object","required":["document"],"properties":{"document":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"tone":{"type":"string","enum":["SENIOR_REVIEWER","STAFF_ENGINEER"],"default":"SENIOR_REVIEWER"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["revisedDocument"],"properties":{"revisedDocument":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '과도하게 단정하지 않으면서 근거, 영향과 권장 조치를 명확히 전달하는 시니어 리뷰 문체로 수정합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='REVIEW_REFINE' AND `version`='v1')
WHERE `block_key` = 'RV-007';

UPDATE `blocks` SET
    `name` = '개발자 전달용으로 출력하기', `description` = '코드 분석 및 리팩토링 결과를 개발 문서로 출력합니다.',
    `input_schema` = '{"type":"object","required":["content"],"properties":{"content":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"format":{"type":"string","enum":["MARKDOWN","HTML"],"default":"MARKDOWN"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["document"],"properties":{"document":{"type":"string"}},"additionalProperties":false}',
    `prompt_instruction` = '요약, 발견된 문제, 개선 코드, 변경 이유, 참고 출처를 개발자가 바로 검토할 수 있도록 구성합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='OUTPUT_DOCUMENT' AND `version`='v1')
WHERE `block_key` = 'OUT-006';

UPDATE `blocks` SET
    `name` = '리팩토링 체크리스트 출력하기', `description` = '리팩토링 항목을 실행 가능한 체크리스트로 출력합니다.',
    `input_schema` = '{"type":"object","required":["findings"],"properties":{"findings":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"groupByCategory":{"type":"boolean","default":true}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["checklist"],"properties":{"checklist":{"type":"array","items":{"type":"object"}}},"additionalProperties":false}',
    `prompt_instruction` = '발견된 문제와 개선안을 우선순위 및 범주별 체크리스트로 구성합니다.',
    `prompt_template_id` = (SELECT `prompt_template_id` FROM `prompt_templates` WHERE `template_key`='OUTPUT_CHECKLIST' AND `version`='v1')
WHERE `block_key` = 'OUT-003';

UPDATE `blocks` SET
    `name` = '내 저장소에 저장하기', `description` = '생성된 결과를 사용자의 저장소에 저장합니다.',
    `input_schema` = '{"type":"object","required":["title","content"],"properties":{"title":{"type":"string","maxLength":200},"content":{"type":"string"}},"additionalProperties":false}',
    `option_schema` = '{"type":"object","properties":{"tags":{"type":"array","items":{"type":"string"}},"folderId":{"type":"string"}},"additionalProperties":false}',
    `output_schema` = '{"type":"object","required":["artifactId","savedAt"],"properties":{"artifactId":{"type":"string"},"savedAt":{"type":"string","format":"date-time"}},"additionalProperties":false}',
    `prompt_instruction` = '생성된 결과를 저장소에 저장하는 후처리 작업을 등록합니다.',
    `default_execution_mode` = 'SYSTEM'
WHERE `block_key` = 'OUT-009';