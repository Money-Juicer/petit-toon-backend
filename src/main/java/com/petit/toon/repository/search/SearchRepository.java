package com.petit.toon.repository.search;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.petit.toon.entity.cartoon.QCartoon.cartoon;
import static com.petit.toon.entity.user.QProfileImage.profileImage;
import static com.petit.toon.entity.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private final JPAQueryFactory queryFactory;

    public List<User> findUser(List<String> keywords, Pageable pageable) {
        return queryFactory.select(user)
                .from(user)
                .join(profileImage).on(profileImage.eq(user.profileImage)).fetchJoin()
                .where(nicknameContainsWith(keywords))
                .orderBy(userSearchRankOrder(keywords.get(0)).asc(), user.createdDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public List<Cartoon> findCartoon(List<String> keywords, Pageable pageable) {
        return queryFactory.select(cartoon)
                .from(cartoon)
                .join(user).on(user.eq(cartoon.user)).fetchJoin()
                .where(titleContainsWith(keywords))
                .orderBy(cartoonSearchRankOrder(keywords.get(0)).asc(), cartoon.createdDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanBuilder nicknameContainsWith(List<String> keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword)) {
                builder.and(user.nickname.contains(keyword));
            }
        }
        return builder;
    }

    private NumberExpression<Integer> userSearchRankOrder(String keyword) {
        return new CaseBuilder()
                .when(user.nickname.eq(keyword)).then(0)
                .when(user.nickname.like(keyword + "%")).then(1)
                .when(user.nickname.like("%" + keyword + "%")).then(2)
                .when(user.nickname.like("%" + keyword)).then(3)
                .otherwise(4);
    }

    private BooleanBuilder titleContainsWith(List<String> keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword)) {
                builder.and(cartoon.title.contains(keyword));
            }
        }
        return builder;
    }

    private NumberExpression<Integer> cartoonSearchRankOrder(String keyword) {
        return new CaseBuilder()
                .when(cartoon.title.eq(keyword)).then(0)
                .when(cartoon.title.like(keyword + "%")).then(1)
                .when(cartoon.title.like("%" + keyword + "%")).then(2)
                .when(cartoon.title.like("%" + keyword)).then(3)
                .otherwise(8);
    }
}
