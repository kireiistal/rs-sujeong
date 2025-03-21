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
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.rsupport.rs_sujeong.domain.notice.entity.QNotice.notice;


@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Notice> search(NoticeSearchCondition condition, Pageable pageable) {
        // 조건 생성
        BooleanBuilder whereCondition = createWhereCondition(condition);

        // 메인 쿼리
        List<Notice> content = queryFactory
                .selectFrom(notice)
                .where(whereCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(createOrderSpecifiers(pageable))
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(notice.count())
                .from(notice)
                .where(whereCondition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder createWhereCondition(NoticeSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        // 검색어 필터링
        applySearchFilter(builder, condition);

        // 날짜 필터링
        applyDateFilter(builder, condition);

        return builder;
    }

    private void applySearchFilter(BooleanBuilder builder, NoticeSearchCondition condition) {
        if (!StringUtils.hasText(condition.getFilter())) {
            return;
        }

        if (NoticeSearchCondition.SearchType.TITLE == condition.getSearchType()) {
            builder.and(notice.title.containsIgnoreCase(condition.getFilter()));
        } else {
            builder.and(
                    notice.title.containsIgnoreCase(condition.getFilter())
                            .or(notice.content.containsIgnoreCase(condition.getFilter()))
            );
        }
    }

    private void applyDateFilter(BooleanBuilder builder, NoticeSearchCondition condition) {
        if (condition.getStartDate() != null) {
            LocalDateTime startDateTime = condition.getStartDate().atStartOfDay();
            builder.and(notice.createdAt.goe(startDateTime));
        }

        if (condition.getEndDate() != null) {
            LocalDateTime endDateTime = condition.getEndDate().atTime(LocalTime.MAX);
            builder.and(notice.createdAt.loe(endDateTime));
        }
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(Pageable pageable) {
        PathBuilder<Notice> entityPath = new PathBuilder<>(Notice.class, "notice");

        return pageable.getSort().stream()
                .map(order -> new OrderSpecifier(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        entityPath.get(order.getProperty())
                ))
                .toArray(OrderSpecifier[]::new);
    }
}