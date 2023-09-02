package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Comment;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.CommentRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.response.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Test
    @DisplayName("댓글 등록")
    void createComment() {
        // given
        User user1 = createUser("Hotoran");
        User user2 = createUser("timel2ss");
        User user3 = createUser("iced");

        Cartoon cartoon1 = createToon("영현이의 모험1");
        Cartoon cartoon2 = createToon("영현이의 모험2");

        // when
        CommentCreateResponse response1 = commentService.createComment(user1.getId(), cartoon1.getId(), "Test Content 1");
        CommentCreateResponse response2 = commentService.createComment(user2.getId(), cartoon1.getId(), "Test Content 2");
        CommentCreateResponse response3 = commentService.createComment(user3.getId(), cartoon2.getId(), "Test Content 3");

        // then
        Comment comment1 = commentRepository.findById(response1.getCommentId()).get();
        Comment comment2 = commentRepository.findById(response2.getCommentId()).get();
        Comment comment3 = commentRepository.findById(response3.getCommentId()).get();

        assertThat(comment1.getUser().getId()).isEqualTo(user1.getId());
        assertThat(comment1.getCartoon().getTitle()).isEqualTo("영현이의 모험1");

        assertThat(comment2.getUser().getId()).isEqualTo(user2.getId());
        assertThat(comment2.getCartoon().getTitle()).isEqualTo("영현이의 모험1");

        assertThat(comment3.getUser().getId()).isEqualTo(user3.getId());
        assertThat(comment3.getCartoon().getTitle()).isEqualTo("영현이의 모험2");
    }

    @Test
    @DisplayName("웹툰 댓글 조회")
    void viewComments() {
        // given
        User user1 = createUser("KIM");
        User user2 = createUser("LEE");
        User user3 = createUser("JIN");

        Cartoon cartoon = createToon("영현이의 모험");

        Comment comment1 = createComment(user1, cartoon);
        Comment comment2 = createComment(user2, cartoon);
        Comment comment3 = createComment(user3, cartoon);
        Comment comment4 = createComment(user3, cartoon); /* user3는 2개 댓글 */

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        CommentListResponse res1 = commentService.viewComments(user1.getId(), cartoon.getId(), pageRequest);
        CommentListResponse res2 = commentService.viewComments(user2.getId(), cartoon.getId(), pageRequest);
        CommentListResponse res3 = commentService.viewComments(user3.getId(), cartoon.getId(), pageRequest);


        // then
        List<CommentResponse> comments1 = res1.getComments();
//        comments1.stream().forEach(comment -> System.out.println(comment.getUserId()));
        assertThat(comments1.size()).isEqualTo(4);
        assertThat(comments1.get(0).isMyComment()).isTrue();
        assertThat(comments1.get(1).isMyComment()).isFalse();

        List<CommentResponse> comments2 = res2.getComments();
//        comments2.stream().forEach(comment -> System.out.println(comment.getUserId()));
        assertThat(comments2.size()).isEqualTo(4);
        assertThat(comments2.get(0).isMyComment()).isTrue();
        assertThat(comments1.get(1).isMyComment()).isFalse();

        List<CommentResponse> comments3 = res3.getComments();
//        comments3.stream().forEach(comment -> System.out.println(comment.getUserId()));
        assertThat(comments3.size()).isEqualTo(4);
        assertThat(comments3.get(0).isMyComment()).isTrue();
        assertThat(comments3.get(1).isMyComment()).isTrue();
    }

    @Test
    @DisplayName("자신이 단 댓글 조회")
    void viewOnlyMyComments() {
        // given
        User user = createUser("Petit");

        Cartoon cartoon1 = createToon("toon 1");
        Cartoon cartoon2 = createToon("toon 2");
        Cartoon cartoon3 = createToon("toon 3");

        Comment comment1 = createComment(user, cartoon1);
        Comment comment2 = createComment(user, cartoon2);
        Comment comment3 = createComment(user, cartoon3);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        MyCommentListResponse res = commentService.viewOnlyMyComments(user.getId(), pageRequest);

        // then
        List<MyCommentResponse> comments = res.getComments();
        assertThat(comments.size()).isEqualTo(3);
        assertThat(comments.get(0).getCartoonId()).isEqualTo(cartoon3.getId());
        assertThat(comments.get(1).getCartoonId()).isEqualTo(cartoon2.getId());
        assertThat(comments.get(2).getCartoonId()).isEqualTo(cartoon1.getId());
    }

    @Test
    @DisplayName("댓글 삭제")
    void removeComment() {
        // given
        User user = createUser("KIM");
        Cartoon cartoon = createToon("sample");
        Comment comment = createComment(user, cartoon);

        //when
        commentService.removeComment(user.getId(), comment.getId());

        //then
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }


    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }

    private Cartoon createToon(String title) {
        return cartoonRepository.save(Cartoon.builder()
                .title(title)
                .build());
    }

    private Comment createComment(User user, Cartoon cartoon) {
        Comment comment = commentRepository.save(Comment.builder()
                .user(user)
                .cartoon(cartoon)
                .content("sample-content")
                .build());
        commentRepository.flush();
        return comment;
    }
}