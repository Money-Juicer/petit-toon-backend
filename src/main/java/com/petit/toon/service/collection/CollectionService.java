package com.petit.toon.service.collection;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.collection.Bookmark;
import com.petit.toon.entity.collection.Collection;
import com.petit.toon.entity.user.User;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.collection.BookmarkRepository;
import com.petit.toon.repository.collection.CollectionRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.collection.response.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final CartoonRepository cartoonRepository;


    /**
     * Collection 생성 서비스
     */
    public CollectionResponse createCollection(long userId, String title, boolean closed) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + userId));

        Collection savedCollection = collectionRepository.save(Collection.builder()
                .user(user)
                .title(title)
                .closed(closed)
                .build());

        return new CollectionResponse(savedCollection.getId());
    }

    /**
     * Bookmark 생성
     */
    public BookmarkResponse createBookmark(long userId, long collectionId, long cartoonId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found. id: " + collectionId));

        if (userId != collection.getUser().getId()) {
            throw new RuntimeException("The user ID in the collection does not match the user ID requested.");
        }

        Cartoon cartoon = cartoonRepository.findById(cartoonId)
                .orElseThrow(() -> new RuntimeException("Cartoon not found. id: " + cartoonId));

        Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
                .collection(collection)
                .cartoon(cartoon)
                .build());

        return new BookmarkResponse(bookmark.getId());
    }

    /**
     * Author의 Collection List 조회
     */
    public CollectionInfoListResponse viewCollectionList(long authorId, boolean isSelf, Pageable pageable) {
        if (isSelf) {
            List<Collection> collections = collectionRepository.findCollectionsByUserId(authorId, pageable);
            List<CollectionInfoResponse> responseList = collections.stream()
                    .map(CollectionInfoResponse::of)
                    .toList();
            return new CollectionInfoListResponse(responseList);
        }
        List<Collection> collections = collectionRepository.findOpenedCollectionsByUserId(authorId, pageable);
        List<CollectionInfoResponse> openedResponseList = collections.stream()
                .map(CollectionInfoResponse::of)
                .toList();
        return new CollectionInfoListResponse(openedResponseList);
    }

    /**
     * Collection의 Bookmark List 조회
     */
    public BookmarkInfoListResponse viewBookmarks(long collectionId, Pageable pageable) {

        List<Bookmark> bookmarks = bookmarkRepository.findBookmarksByCollectionId(collectionId, pageable);
        List<BookmarkInfoResponse> bookmarkInfos = bookmarks.stream()
                .map(BookmarkInfoResponse::of)
                .toList();
        return new BookmarkInfoListResponse(bookmarkInfos);
    }

    public void removeCollection(long collectionId) {
        collectionRepository.deleteById(collectionId);
    }

    public void removeBookmark(long bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }
}
