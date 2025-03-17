-- 공지사항 테이블 생성
CREATE TABLE notice
(
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    start_date TIMESTAMP    NOT NULL,
    end_date   TIMESTAMP    NOT NULL,
    view_count BIGINT    DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)  NOT NULL,
    is_deleted BOOLEAN   DEFAULT FALSE
);

-- 인덱스 생성
CREATE INDEX idx_notice_created_at ON notice (created_at);

COMMENT
    ON TABLE notice IS '공지사항';
COMMENT
    ON COLUMN notice.id IS '공지사항 고유 식별자';
COMMENT
    ON COLUMN notice.title IS '공지사항 제목';
COMMENT
    ON COLUMN notice.content IS '공지사항 내용';
COMMENT
    ON COLUMN notice.start_date IS '공지 시작 일시';
COMMENT
    ON COLUMN notice.end_date IS '공지 종료 일시';
COMMENT
    ON COLUMN notice.view_count IS '조회수';
COMMENT
    ON COLUMN notice.created_at IS '등록 일시';
COMMENT
    ON COLUMN notice.updated_at IS '수정 일시';
COMMENT
    ON COLUMN notice.created_by IS '작성자';
COMMENT
    ON COLUMN notice.is_deleted IS '삭제 여부 (true: 삭제됨, false: 활성화)';

-- 공지사항 첨부파일 테이블 생성
CREATE TABLE notice_file
(
    id                BIGSERIAL PRIMARY KEY,
    notice_id         BIGINT       NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename   VARCHAR(255) NOT NULL,
    file_size         BIGINT       NOT NULL,
    file_type         VARCHAR(100),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notice_file_notice FOREIGN KEY (notice_id) REFERENCES notice (id) ON DELETE CASCADE
);

COMMENT
    ON TABLE notice_file IS '공지사항 첨부파일';
COMMENT
    ON COLUMN notice_file.id IS '첨부파일 고유 식별자';
COMMENT
    ON COLUMN notice_file.notice_id IS '연결된 공지사항 ID';
COMMENT
    ON COLUMN notice_file.original_filename IS '원본 파일명';
COMMENT
    ON COLUMN notice_file.stored_filename IS '저장된 파일명 (UUID)';
COMMENT
    ON COLUMN notice_file.file_size IS '파일 크기 (바이트)';
COMMENT
    ON COLUMN notice_file.file_type IS '파일 MIME 타입';
COMMENT
    ON COLUMN notice_file.created_at IS '등록 일시';

