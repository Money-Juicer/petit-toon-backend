package com.petit.toon.repository.rank;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.petit.toon.entity.cartoon.QCartoon.cartoon;
import static com.petit.toon.entity.cartoon.QLike.like;
import static com.petit.toon.entity.user.QFollow.follow;
import static com.petit.toon.entity.user.QUser.user;

@Repository
@RequiredArgsConstructor
public class RankRepository {

    private final JPAQueryFactory queryFactory;

    public List<Long> findUserRank(Pageable pageable, LocalDateTime date) {
        return queryFactory.select(user.id)
                .from(user)
                .join(follow).on(follow.followee.eq(user))
                .where(follow.createdDateTime.goe(date))
                .groupBy(user)
                .orderBy(follow.count().desc(), user.createdDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public List<Long> findCartoonRank(Pageable pageable, LocalDateTime date) {
        return queryFactory.select(cartoon.id)
                .from(cartoon)
                .join(like).on(cartoon.id.eq(like.cartoon.id))
                .where(like.createdDateTime.goe(date))
                .groupBy(cartoon)
                .orderBy(like.count().desc(), cartoon.createdDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
