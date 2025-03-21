package com.rsupport.rs_sujeong.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeSearchCondition {
    private String filter;
    private SearchType searchType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    public enum SearchType {
        TITLE, TITLE_CONTENT
    }

    @Override
    public String toString() {
        return "filter=" + (filter == null ? "" : filter) +
                ";searchType=" + (searchType == null ? "" : searchType) +
                ";startDate=" + (startDate == null ? "" : startDate) +
                ";endDate=" + (endDate == null ? "" : endDate);
    }
}
