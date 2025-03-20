package com.rsupport.rs_sujeong.domain.notice.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rsupport.rs_sujeong.domain.notice.dto.NoticeSearchCondition;
import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.rsupport.rs_sujeong.domain.notice.entity.QNotice.notice;


@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Notice> search(NoticeSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = queryBuilder(condition);
        JPAQuery<Notice> query = queryFactory
                .selectFrom(notice)
                .where(builder)
                .orderBy(notice.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        PathBuilder<Notice> orderBy = new PathBuilder<>(Notice.class, notice.getMetadata());
        for (Sort.Order order : pageable.getSort()) {
            query.orderBy(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, orderBy.get(order.getProperty())));
        }

        JPAQuery<Long> countQuery = queryFactory
                .select(notice.count())
                .from(notice)
                .where(builder);

        return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchOne);
    }


    private BooleanBuilder queryBuilder(NoticeSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(condition.getFilter())) {
            if (NoticeSearchCondition.SearchType.TITLE == condition.getSearchType()) {
                builder.and(notice.title.containsIgnoreCase(condition.getFilter()));
            } else {
                builder.and(notice.title.containsIgnoreCase(condition.getFilter()).or(notice.content.containsIgnoreCase(condition.getFilter())));
            }
        }

        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            builder.and(notice.createdAt.goe(startDateTime));
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            builder.and(notice.createdAt.loe(endDateTime));
        }

        return builder;
    }
}
