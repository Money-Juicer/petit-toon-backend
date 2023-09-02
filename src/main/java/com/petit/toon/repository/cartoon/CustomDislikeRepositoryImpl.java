package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Dislike;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomDislikeRepositoryImpl implements CustomDislikeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsert(List<Dislike> dislikes) {
        jdbcTemplate.batchUpdate(
                "insert into dislikes(user_id, cartoon_id) " +
                        "values (?, ?)",
                getBatchPreparedStatementSetter(dislikes)
        );
    }

    private BatchPreparedStatementSetter getBatchPreparedStatementSetter(List<Dislike> dislikes) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, dislikes.get(i).getUser().getId());
                ps.setLong(2, dislikes.get(i).getCartoon().getId());
            }

            @Override
            public int getBatchSize() {
                return dislikes.size();
            }
        };
    }
}
