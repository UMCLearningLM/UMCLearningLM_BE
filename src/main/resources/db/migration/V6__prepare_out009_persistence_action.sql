-- OUT-009 is a local persistence action. It selects completed AI outputs
-- instead of receiving generated content before the model call.
UPDATE `blocks`
SET `input_schema` = '{"type":"object","required":["title","sourceOutputKeys"],"properties":{"title":{"type":"string","maxLength":200},"sourceOutputKeys":{"type":"array","minItems":1,"uniqueItems":true,"items":{"type":"string"}}},"additionalProperties":false}',
    `prompt_template_id` = NULL,
    `default_execution_mode` = 'SYSTEM'
WHERE `block_key` = 'OUT-009';
