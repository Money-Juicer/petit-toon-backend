package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.petit.toon.entity.cartoon.QCartoon.cartoon;
import static com.petit.toon.entity.user.QFollow.follow;
import static com.petit.toon.entity.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class CustomCartoonRepositoryImpl implements CustomCartoonRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Cartoon> findAllWithExactOrder(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory.select(cartoon)
                .from(cartoon)
                .where(cartoon.id.in(ids))
                .orderBy(orderByIdsExactOrder(ids).asc())
                .fetch();
    }

    @Override
    public List<Cartoon> findAllWithUserWithExactOrder(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory.select(cartoon)
                .from(cartoon)
                .join(user).on(cartoon.user.eq(user)).fetchJoin()
                .where(cartoon.id.in(ids))
                .orderBy(orderByIdsExactOrder(ids).asc())
                .fetch();
    }

    @Override
    public List<Cartoon> findAllWithFollower(Long userId, Pageable pageable) {
        return queryFactory.select(cartoon)
                .from(cartoon)
                .join(follow).on(follow.followee.eq(cartoon.user))
                .join(user).on(follow.follower.eq(user))
                .where(cartoon.user.isInfluencer.and(user.id.eq(userId)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(cartoon.createdDateTime.desc())
                .fetch();
    }

    private NumberExpression<Integer> orderByIdsExactOrder(List<Long> ids) {
        CaseBuilder caseBuilder = new CaseBuilder();
        CaseBuilder.Cases<Integer, NumberExpression<Integer>> chain =
                caseBuilder.when(cartoon.id.eq(ids.get(0))).then(0);
        for (int i = 1; i < ids.size(); i++) {
            chain = chain.when(cartoon.id.eq(ids.get(i))).then(i);
        }
        return chain.otherwise(Integer.MAX_VALUE);
    }
}
