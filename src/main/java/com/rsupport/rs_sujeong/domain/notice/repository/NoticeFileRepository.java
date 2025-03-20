package com.rsupport.rs_sujeong.domain.notice.repository;


import com.rsupport.rs_sujeong.domain.notice.entity.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
    void deleteAllByIdIn(List<Long> ids);
}
