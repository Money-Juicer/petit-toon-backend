package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomLikeRepositoryImpl implements CustomLikeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsert(List<Like> likes) {
        jdbcTemplate.batchUpdate(
                "insert into likes(user_id, cartoon_id) " +
                        "values (?, ?)",
                getBatchPreparedStatementSetter(likes)
        );
    }

    private BatchPreparedStatementSetter getBatchPreparedStatementSetter(List<Like> likes) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, likes.get(i).getUser().getId());
                ps.setLong(2, likes.get(i).getCartoon().getId());
            }

            @Override
            public int getBatchSize() {
                return likes.size();
            }
        };
    }
}
