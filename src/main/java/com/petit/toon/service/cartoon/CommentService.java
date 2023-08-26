package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Comment;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.AuthorityNotMatchException;
import com.petit.toon.exception.notfound.CartoonNotFoundException;
import com.petit.toon.exception.notfound.CommentNotFoundException;
import com.petit.toon.exception.notfound.UserNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.cartoon.CommentRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.cartoon.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CartoonRepository cartoonRepository;
    private final UserRepository userRepository;

    public CommentCreateResponse createComment(long userId, long cartoonId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Cartoon cartoon = cartoonRepository.findById(cartoonId)
                .orElseThrow(CartoonNotFoundException::new);

        Comment comment = commentRepository.save(Comment.builder()
                .user(user)
                .cartoon(cartoon)
                .content(content)
                .build());

        return new CommentCreateResponse(comment.getId());
    }

    public CommentListResponse viewComments(long userId, long cartoonId, Pageable pageable) {
        List<Comment> comments = commentRepository.findCommentsByUserIdAndCartoonId(userId, cartoonId, pageable);

        List<CommentResponse> commentResponses = comments.stream()
                .map(c -> CommentResponse.of(c, (userId == c.getUser().getId())))
                .toList();

        return new CommentListResponse(commentResponses);
    }

    public MyCommentListResponse viewOnlyMyComments(long userId, Pageable pageable) {
        List<Comment> comments = commentRepository.findCommentsByUserId(userId, pageable);

        List<MyCommentResponse> commentResponses = comments.stream()
                .map(MyCommentResponse::of)
                .toList();

        return new MyCommentListResponse(commentResponses);
    }

    public void removeComment(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        if (comment.getUser().getId() != userId) {
            throw new AuthorityNotMatchException();
        }

        commentRepository.deleteById(commentId);
    }
}
