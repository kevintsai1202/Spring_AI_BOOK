-- 企業資料庫初始化腳本
-- 模擬企業環境中的員工、部門、產品等資料

-- 創建部門表
CREATE TABLE IF NOT EXISTS departments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    manager_name VARCHAR(100),
    location VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 創建員工表
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department_id INTEGER REFERENCES departments(id),
    position VARCHAR(100),
    salary DECIMAL(10, 2),
    hire_date DATE,
    phone VARCHAR(20),
    address TEXT,
    skills TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 創建產品表
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    product_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    price DECIMAL(10, 2),
    stock_quantity INTEGER,
    supplier VARCHAR(200),
    specifications JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 創建專案表
CREATE TABLE IF NOT EXISTS projects (
    id SERIAL PRIMARY KEY,
    project_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    department_id INTEGER REFERENCES departments(id),
    budget DECIMAL(12, 2),
    start_date DATE,
    end_date DATE,
    status VARCHAR(50),
    manager_id INTEGER REFERENCES employees(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入部門測試資料
INSERT INTO departments (name, description, manager_name, location) VALUES
('研發部', '負責產品研發和技術創新', '張經理', '台北總部 5F'),
('業務部', '負責市場開發和客戶服務', '王經理', '台北總部 3F'),
('人力資源部', '負責人才招募和員工發展', '李經理', '台北總部 2F'),
('財務部', '負責財務管理和成本控制', '陳經理', '台北總部 4F'),
('IT 部門', '負責資訊系統維護和開發', '林經理', '台北總部 6F')
ON CONFLICT DO NOTHING;

-- 插入員工測試資料
INSERT INTO employees (employee_id, name, email, department_id, position, salary, hire_date, phone, skills) VALUES
('EMP001', '王小明', 'wang.xiaoming@company.com', 1, 'Senior Developer', 80000, '2020-03-15', '0912-345-678', 'Java, Spring Boot, PostgreSQL, Neo4j'),
('EMP002', '李美玲', 'li.meiling@company.com', 1, 'AI Engineer', 90000, '2019-06-01', '0923-456-789', 'Python, Machine Learning, TensorFlow, RAG'),
('EMP003', '陳大偉', 'chen.dawei@company.com', 2, 'Sales Manager', 75000, '2018-09-10', '0934-567-890', 'CRM, Sales Strategy, Negotiation'),
('EMP004', '劉志強', 'liu.zhiqiang@company.com', 5, 'DevOps Engineer', 85000, '2021-01-20', '0945-678-901', 'Docker, Kubernetes, CI/CD, Prometheus'),
('EMP005', '黃淑芬', 'huang.shufen@company.com', 3, 'HR Specialist', 65000, '2022-04-01', '0956-789-012', 'Recruitment, Training, Employee Relations'),
('EMP006', '張志明', 'zhang.zhiming@company.com', 1, 'Data Scientist', 95000, '2020-07-15', '0967-890-123', 'Python, R, Data Analysis, Visualization'),
('EMP007', '林雅婷', 'lin.yating@company.com', 4, 'Financial Analyst', 70000, '2021-11-01', '0978-901-234', 'Excel, Financial Modeling, Budgeting'),
('EMP008', '吳建宏', 'wu.jianhong@company.com', 5, 'System Architect', 100000, '2017-05-20', '0989-012-345', 'System Design, Cloud Architecture, Microservices'),
('EMP009', '鄭美惠', 'zheng.meihui@company.com', 2, 'Business Analyst', 72000, '2020-10-15', '0990-123-456', 'Business Analysis, Requirements Gathering, SQL'),
('EMP010', '周俊傑', 'zhou.junjie@company.com', 1, 'Backend Developer', 78000, '2021-03-01', '0901-234-567', 'Node.js, Express, MongoDB, REST API')
ON CONFLICT DO NOTHING;

-- 插入產品測試資料
INSERT INTO products (product_code, name, category, description, price, stock_quantity, supplier, specifications) VALUES
('PROD-001', 'AI 智能助理系統', '軟體產品', '基於 Spring AI 的企業級智能助理解決方案，支援 RAG 技術', 500000, 10, '內部研發', '{"version": "2.0", "license": "Enterprise", "support": "24/7"}'),
('PROD-002', '資料分析平台', '軟體產品', '整合多種資料源的企業資料分析平台', 300000, 15, '內部研發', '{"version": "1.5", "databases": ["PostgreSQL", "Neo4j"], "features": ["Dashboard", "Report"]}'),
('PROD-003', 'DevOps 自動化工具', '軟體產品', 'CI/CD 流程自動化和容器編排工具', 200000, 20, '內部研發', '{"version": "3.0", "containers": "Docker", "orchestration": "Kubernetes"}'),
('PROD-004', '客戶關係管理系統', '軟體產品', '完整的 CRM 解決方案，支援銷售流程管理', 400000, 8, '外部供應商', '{"version": "4.2", "modules": ["Sales", "Marketing", "Service"], "integration": "API"}'),
('PROD-005', '人力資源管理系統', '軟體產品', 'HR 全流程管理，包含招聘、薪資、績效', 350000, 12, '外部供應商', '{"version": "2.8", "modules": ["Recruitment", "Payroll", "Performance"]}')
ON CONFLICT DO NOTHING;

-- 插入專案測試資料
INSERT INTO projects (project_code, name, description, department_id, budget, start_date, end_date, status, manager_id) VALUES
('PRJ-2024-001', 'Spring AI RAG 整合專案', '將 RAG 技術整合到現有產品線，提升智能助理能力', 1, 5000000, '2024-01-01', '2024-12-31', 'In Progress', 2),
('PRJ-2024-002', '企業資料湖建設', '建立統一的企業資料湖，整合各部門資料', 5, 8000000, '2024-03-01', '2025-02-28', 'In Progress', 8),
('PRJ-2024-003', '客戶體驗優化計劃', '優化客戶服務流程，提升客戶滿意度', 2, 3000000, '2024-05-01', '2024-11-30', 'In Progress', 3),
('PRJ-2023-004', '人才培訓體系升級', '建立系統化的員工培訓和發展體系', 3, 2000000, '2023-07-01', '2024-06-30', 'Completed', 5),
('PRJ-2024-005', '雲端架構遷移', '將現有系統遷移到雲端平台', 5, 10000000, '2024-04-01', '2025-03-31', 'In Progress', 4)
ON CONFLICT DO NOTHING;

-- 創建索引以提升查詢效能
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees(department_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON employees(email);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_department ON projects(department_id);

-- 創建更新時間戳的函數
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 為各表添加自動更新時間戳的觸發器
CREATE TRIGGER update_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
