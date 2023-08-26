package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.cartoon.request.CommentCreateRequest;
import com.petit.toon.service.cartoon.CommentService;
import com.petit.toon.service.cartoon.response.CommentCreateResponse;
import com.petit.toon.service.cartoon.response.CommentListResponse;
import com.petit.toon.service.cartoon.response.MyCommentListResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/api/v1/comment/{toonId}")
    public ResponseEntity<CommentCreateResponse> enrollComment(@AuthenticationPrincipal(expression = "user.id") long userId,
                                                               @PathVariable("toonId") long toonId,
                                                               @Valid @RequestBody CommentCreateRequest request) {

        CommentCreateResponse response = commentService.createComment(userId, toonId, request.getContent());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/comment/{toonId}")
    public ResponseEntity<CommentListResponse> listComments(@AuthenticationPrincipal(expression = "user.id") long userId,
                                                            @PathVariable("toonId") long toonId,
                                                            @PageableDefault Pageable pageable) {

        CommentListResponse response = commentService.viewComments(userId, toonId, pageable);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/comment/myComment")
    public ResponseEntity<MyCommentListResponse> listUserComments(@AuthenticationPrincipal(expression = "user.id") long userId,
                                                                  @PageableDefault Pageable pageable) {
        MyCommentListResponse response = commentService.viewOnlyMyComments(userId, pageable);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("/api/v1/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal(expression = "user.id") long userId,
                                              @PathVariable("commentId") long commentId) {
        commentService.removeComment(userId, commentId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
