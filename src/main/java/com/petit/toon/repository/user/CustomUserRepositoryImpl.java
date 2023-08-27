package com.petit.toon.repository.user;

import com.petit.toon.entity.user.User;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.petit.toon.entity.user.QProfileImage.profileImage;
import static com.petit.toon.entity.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> findAllWithProfileImageWithExactOrder(List<Long> ids) {
        return queryFactory.select(user)
                .from(user)
                .join(profileImage).on(user.profileImage.eq(profileImage)).fetchJoin()
                .where(user.id.in(ids))
                .orderBy(orderByIdsExactOrder(ids).asc())
                .fetch();
    }

    private NumberExpression<Integer> orderByIdsExactOrder(List<Long> ids) {
        CaseBuilder caseBuilder = new CaseBuilder();
        CaseBuilder.Cases<Integer, NumberExpression<Integer>> chain =
                caseBuilder.when(user.id.eq(ids.get(0))).then(0);
        for (int i = 1; i < ids.size(); i++) {
            chain = chain.when(user.id.eq(ids.get(i))).then(i);
        }
        return chain.otherwise(Integer.MAX_VALUE);
    }
}
