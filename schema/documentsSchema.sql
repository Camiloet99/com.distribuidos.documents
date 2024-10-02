CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NULL,
    description TEXT NULL,
    verified BOOLEAN NULL,
    download_link VARCHAR(100) NOT NULL,
    user_document_id BIGINT NOT NULL,
    creation_date DATETIME NULL
);