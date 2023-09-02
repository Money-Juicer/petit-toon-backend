package com.petit.toon.repository.user;

import com.petit.toon.entity.user.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static com.petit.toon.entity.user.QFollow.follow;
import static com.petit.toon.entity.user.QProfileImage.profileImage;
import static com.petit.toon.entity.user.QUser.user;
import static com.petit.toon.service.user.ProfileImageService.DEFAULT_PROFILE_IMAGE_ID;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAllWithProfileImageWithExactOrder(List<Long> ids) {
        return queryFactory.select(user)
                .from(user)
                .join(profileImage).on(user.profileImage.eq(profileImage)).fetchJoin()
                .where(user.id.in(ids))
                .orderBy(orderByIdsExactOrder(ids).asc())
                .fetch();
    }

    @Override
    public long updateInfluenceStatus(boolean status) {
        return queryFactory.update(user)
                .set(user.isInfluencer, status)
                .where(user.id.in(
                        JPAExpressions.select(follow.followee.id)
                                .from(follow)
                                .groupBy(follow.followee)
                                .having(checkFollowCountWithBoundary(status))
                ))
                .execute();
    }

    @Override
    public void bulkInsert(List<User> users) {
        jdbcTemplate.batchUpdate(
                "insert into users(name, nickname, tag, email, password, profile_image_id, status_message, is_influencer) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?)",
                getBatchPreparedStatementSetter(users)
        );
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

    private BooleanExpression checkFollowCountWithBoundary(boolean status) {
        int boundary = 10_000;
        return status
                ? follow.count().goe(boundary)
                : follow.count().lt(boundary);
    }

    private BatchPreparedStatementSetter getBatchPreparedStatementSetter(List<User> users) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, users.get(i).getName());
                ps.setString(2, users.get(i).getNickname());
                ps.setString(3, users.get(i).getTag());
                ps.setString(4, users.get(i).getEmail());
                ps.setString(5, users.get(i).getPassword());
                ps.setLong(6, DEFAULT_PROFILE_IMAGE_ID);
                ps.setString(7, users.get(i).getStatusMessage());
                ps.setBoolean(8, users.get(i).isInfluencer());
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        };
    }
}
