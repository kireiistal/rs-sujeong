package com.rsupport.rs_sujeong.domain.notice.repository;


import com.rsupport.rs_sujeong.domain.notice.dto.NoticeSearchCondition;
import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {
    Page<Notice> search(NoticeSearchCondition condition, Pageable pageable);
}