-- Spring AI MCP Server Advanced - 初始化數據
-- 提示模板和補全數據的初始化

-- =============================================================================
-- 提示模板數據
-- =============================================================================

-- 插入基本提示模板
INSERT INTO prompt_template (id, name, description, template, created_at) VALUES
(1, 'query-tech', 'Technology query template',
 'What would you like to know about {topic}? I can help explain technical concepts.',
 CURRENT_TIMESTAMP),

(2, 'query-travel', 'Travel query template',
 'Tell me about traveling to {destination}. I''ll provide information about attractions, culture, and tips.',
 CURRENT_TIMESTAMP),

(3, 'code-review-template', 'Code review template',
 'Please review the following {language} code and provide feedback on:\n- Code quality\n- Best practices\n- Potential issues',
 CURRENT_TIMESTAMP),

(4, 'learning-path-template', 'Learning path template',
 'Create a learning path for {topic} at {level} level. Include prerequisites, main concepts, and resources.',
 CURRENT_TIMESTAMP);

-- =============================================================================
-- 提示參數數據（如果需要獨立的參數表）
-- 注意：在當前設計中，參數存儲在 prompt_template 的 parameters 欄位中（ElementCollection）
-- =============================================================================

-- =============================================================================
-- 補全數據
-- =============================================================================

-- 用戶名補全數據
INSERT INTO completion_data (category, values) VALUES
('username', '["alice", "andrew", "amanda", "bob", "betty", "brian", "charlie", "chris", "carol", "david", "diana", "daniel", "emma", "eric", "emily", "frank", "fiona", "fred", "grace", "george", "gina", "henry", "hannah", "helen", "irene", "ivan", "iris", "john", "jane", "jack", "kevin", "karen", "kate", "lisa", "leo", "laura", "mary", "mike", "maria", "nancy", "nick", "nina", "oliver", "olivia", "oscar", "paul", "paula", "peter", "quinn", "quentin", "rachel", "robert", "rose", "sam", "sarah", "steve", "tom", "tina", "tony", "uma", "ursula", "victor", "victoria", "vince", "wendy", "walter", "william", "xavier", "xena", "yara", "yuki", "yvonne", "zach", "zoe", "zara"]'),

-- 國家名補全數據
('country', '["Afghanistan", "Albania", "Algeria", "Argentina", "Australia", "Austria", "Bangladesh", "Belgium", "Brazil", "Canada", "Chile", "China", "Colombia", "Cuba", "Denmark", "Egypt", "Ethiopia", "Finland", "France", "Germany", "Ghana", "Greece", "Guatemala", "Haiti", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kenya", "Kuwait", "Lebanon", "Libya", "Malaysia", "Mexico", "Morocco", "Nepal", "Netherlands", "New Zealand", "Nigeria", "Norway", "Pakistan", "Panama", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Saudi Arabia", "Senegal", "Singapore", "Somalia", "South Africa", "South Korea", "Spain", "Sri Lanka", "Sudan", "Sweden", "Switzerland", "Syria", "Taiwan", "Thailand", "Tunisia", "Turkey", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"]'),

-- 程式語言補全數據
('language', '["Java", "JavaScript", "Python", "C++", "C#", "Go", "Rust", "TypeScript", "PHP", "Ruby", "Swift", "Kotlin", "Scala", "Perl", "R", "MATLAB", "Dart", "Lua", "Haskell", "Elixir"]'),

-- 技能等級補全數據
('level', '["beginner", "intermediate", "advanced", "expert"]'),

-- 興趣類別補全數據
('interests', '["coding", "reading", "traveling", "sports", "music", "art", "gaming", "cooking", "photography", "writing", "dancing", "hiking", "cycling", "swimming", "yoga", "meditation", "gardening", "volunteering", "learning languages", "watching movies"]');

-- =============================================================================
-- 提示模板參數關聯（如果使用獨立的參數表）
-- 注意：在當前設計中不需要，因為使用 @ElementCollection
-- =============================================================================

-- =============================================================================
-- 驗證數據
-- =============================================================================

-- 查詢確認數據已插入
-- SELECT * FROM prompt_template;
-- SELECT * FROM completion_data;
