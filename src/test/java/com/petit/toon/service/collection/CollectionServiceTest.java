package com.petit.toon.service.collection;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.collection.Bookmark;
import com.petit.toon.entity.collection.Collection;
import com.petit.toon.entity.user.User;
import com.petit.toon.exception.badrequest.AuthorityNotMatchException;
import com.petit.toon.exception.notfound.BookmarkNotFoundException;
import com.petit.toon.exception.notfound.CollectionNotFoundException;
import com.petit.toon.repository.cartoon.CartoonRepository;
import com.petit.toon.repository.collection.BookmarkRepository;
import com.petit.toon.repository.collection.CollectionRepository;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.collection.response.BookmarkInfoListResponse;
import com.petit.toon.service.collection.response.BookmarkResponse;
import com.petit.toon.service.collection.response.CollectionInfoListResponse;
import com.petit.toon.service.collection.response.CollectionResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CollectionServiceTest {

    @Autowired
    CollectionService collectionService;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    CartoonRepository cartoonRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void createCollection() {
        // given
        User user1 = createUser("지훈");
        User user2 = createUser("David");

        // when
        CollectionResponse response1 = collectionService.createCollection(user1.getId(), "collection1-title", false);
        CollectionResponse response2 = collectionService.createCollection(user2.getId(), "collection2-title", true);

        // then
        Collection collection1 = collectionRepository.findById(response1.getCollectionId()).get();
        assertThat(collection1.getTitle()).isEqualTo("collection1-title");
        assertThat(collection1.isClosed()).isFalse();
        assertThat(collection1.getUser().getNickname()).isEqualTo("지훈");

        Collection collection2 = collectionRepository.findById(response2.getCollectionId()).get();
        assertThat(collection2.getTitle()).isEqualTo("collection2-title");
        assertThat(collection2.isClosed()).isTrue();
        assertThat(collection2.getUser().getNickname()).isEqualTo("David");


    }

    @Test
    void createBookmark() {
        // given
        User user1 = createUser("영현");
        User user2 = createUser("ICED");
        Collection collection = collectionRepository.save(Collection.builder()
                .user(user1)
                .title("영현이컬렉션")
                .closed(false)
                .build());
        Cartoon cartoon1 = createToon(user1, "sample-title1", "sample/thumbnail/path");
        Cartoon cartoon2 = createToon(user2, "sample-title2", "sample/thumbnail/path");

        // when
        BookmarkResponse response1 = collectionService.createBookmark(user1.getId(), collection.getId(), cartoon1.getId());
        BookmarkResponse response2 = collectionService.createBookmark(user1.getId(), collection.getId(), cartoon2.getId());


        // then
        Bookmark bookmark1 = bookmarkRepository.findById(response1.getBookmarkId()).get();
        assertThat(bookmark1.getCollection().getTitle()).isEqualTo("영현이컬렉션");
        assertThat(bookmark1.getCollection().isClosed()).isFalse();
        assertThat(bookmark1.getCartoon().getTitle()).isEqualTo("sample-title1");

        Bookmark bookmark2 = bookmarkRepository.findById(response2.getBookmarkId()).get();
        assertThat(bookmark2.getCollection().getTitle()).isEqualTo("영현이컬렉션");
        assertThat(bookmark2.getCollection().isClosed()).isFalse();
        assertThat(bookmark2.getCartoon().getTitle()).isEqualTo("sample-title2");

    }

    @Test
    void viewCollectionList() {
        // given
        User user1 = createUser("용우");
        User user2 = createUser("승환");
        Collection collection1 = collectionRepository.save(Collection.builder()
                .user(user1)
                .title("용우의컬렉션")
                .closed(false)
                .build());

        Collection collection2 = collectionRepository.save(Collection.builder()
                .user(user1)
                .title("용우의김영한컬렉션")
                .closed(false)
                .build());

        Collection collection3 = collectionRepository.save(Collection.builder()
                .user(user1)
                .title("용우의은밀한컬렉션")
                .closed(true)
                .build());

        PageRequest pageRequest = PageRequest.of(0, 30);

        // when
        /** 용우 -> 용우 (자신의 컬렉션 리스트 조회) **/
        CollectionInfoListResponse openResponse = collectionService.viewCollectionList(user1.getId(), true, pageRequest);
        /** 다른사람 -> 용우의 컬렉션 조회 (비공개 컬렉션은 Response 담겨있으면 안됨.)**/
        CollectionInfoListResponse closedResponse = collectionService.viewCollectionList(user1.getId(), false, pageRequest);
        /** 다른사람 -> 승환 (아무 컬렉션도 담겨있지 않음)**/
        CollectionInfoListResponse nothingResponse = collectionService.viewCollectionList(user2.getId(), false, pageRequest);

        // then
        /** openResponse **/
        assertThat(openResponse.getCollectionInfos().size()).isEqualTo(3);
        assertThat(openResponse.getCollectionInfos()).extracting("id", "title", "closed")
                .contains(
                        tuple(collection1.getId(), collection1.getTitle(), false),
                        tuple(collection2.getId(), collection2.getTitle(), false),
                        tuple(collection3.getId(), collection3.getTitle(), true)
                );
        /** closedResponse **/
        assertThat(closedResponse.getCollectionInfos().size()).isEqualTo(2);
        assertThat(closedResponse.getCollectionInfos()).extracting("id", "title", "closed")
                .contains(
                        tuple(collection1.getId(), collection1.getTitle(), false),
                        tuple(collection2.getId(), collection2.getTitle(), false)
                );

        /** nothingResponse **/
        assertThat(nothingResponse.getCollectionInfos().size()).isEqualTo(0);
    }

    @Test
    void viewBookmarks() {
        // given
        User user1 = createUser("민서");
        User user2 = createUser("Jin");
        Collection collection = collectionRepository.save(Collection.builder()
                .user(user1)
                .title("민서의컬렉션")
                .closed(false)
                .build());

        Cartoon cartoon1 = createToon(user1, "sample-title1", "sample/thumbnail/path");
        Cartoon cartoon2 = createToon(user1, "sample-title2", "sample/thumbnail/path");
        Cartoon cartoon3 = createToon(user2, "sample-title3", "sample/thumbnail/path");

        Bookmark bookmark1 = createBookmark(collection, cartoon1);
        Bookmark bookmark2 = createBookmark(collection, cartoon2);
        Bookmark bookmark3 = createBookmark(collection, cartoon3);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        BookmarkInfoListResponse response = collectionService.viewBookmarks(collection.getId(), pageRequest);

        // then
        assertThat(response.getBookmarkInfos().size()).isEqualTo(3);
        assertThat(response.getBookmarkInfos()).extracting("bookmarkId", "cartoonId", "cartoonTitle", "thumbnailPath")
                .contains(
                        tuple(bookmark1.getId(), cartoon1.getId(), cartoon1.getTitle(), cartoon1.getThumbnailPath()),
                        tuple(bookmark2.getId(), cartoon2.getId(), cartoon2.getTitle(), cartoon2.getThumbnailPath()),
                        tuple(bookmark3.getId(), cartoon3.getId(), cartoon3.getTitle(), cartoon3.getThumbnailPath())
                );


    }

    @Test
    void removeCollection() {
        // given
        User user = createUser("LEE");
        Collection collection = collectionRepository.save(Collection.builder()
                .user(user)
                .title("LEE 컬렉션")
                .closed(false)
                .build());

        // when
        collectionService.removeCollection(user.getId(), collection.getId());

        // then
        List<Collection> collections = collectionRepository.findAll();
        assertThat(collections).isEmpty();
    }

    @Test
    @DisplayName("removeCollection - 삭제 권한이 없는 경우")
    void removeCollection2() {
        // given
        User user = createUser("LEE");
        User user2 = createUser("KIM");
        Collection collection = collectionRepository.save(Collection.builder()
                .user(user)
                .title("LEE 컬렉션")
                .closed(false)
                .build());

        // when // then
        assertThatThrownBy(() -> collectionService.removeCollection(user2.getId(), collection.getId()))
                .isInstanceOf(AuthorityNotMatchException.class)
                .hasMessage(AuthorityNotMatchException.MESSAGE);
    }

    @Test
    @DisplayName("removeCollection - collection이 존재하지 않는 경우")
    void removeCollection3() {
        // when // then
        assertThatThrownBy(() -> collectionService.removeCollection(1L, 99999L))
                .isInstanceOf(CollectionNotFoundException.class)
                .hasMessage(CollectionNotFoundException.MESSAGE);
    }

    @Test
    void removeBookmark() {
        // given
        User user = createUser("KIM");
        Collection collection = collectionRepository.save(Collection.builder()
                .user(user)
                .title("KIM 컬렉션")
                .closed(false)
                .build());

        Cartoon cartoon = createToon(user, "sample-title", "sample/thumbnail/path");

        Bookmark bookmark = createBookmark(collection, cartoon);

        // when
        collectionService.removeBookmark(user.getId(), bookmark.getId());

        // then
        List<Bookmark> bookmarks = bookmarkRepository.findAll();
        assertThat(bookmarks).isEmpty();
    }

    @Test
    @DisplayName("removeBookmark - 삭제 권한이 없는 경우")
    void removeBookmark2() {
        // given
        User user = createUser("LEE");
        User user2 = createUser("KIM");
        Collection collection = collectionRepository.save(Collection.builder()
                .user(user)
                .title("LEE 컬렉션")
                .closed(false)
                .build());

        Cartoon toon = createToon(user, "title", "path");
        Bookmark bookmark = createBookmark(collection, toon);

        // when // then
        assertThatThrownBy(() -> collectionService.removeBookmark(user2.getId(), bookmark.getId()))
                .isInstanceOf(AuthorityNotMatchException.class)
                .hasMessage(AuthorityNotMatchException.MESSAGE);
    }

    @Test
    @DisplayName("removeBookmark - Bookmark가 존재하지 않는 경우")
    void removeBookmark3() {
        // when // then
        assertThatThrownBy(() -> collectionService.removeBookmark(1L, 99999L))
                .isInstanceOf(BookmarkNotFoundException.class)
                .hasMessage(BookmarkNotFoundException.MESSAGE);
    }

    private User createUser(String nickname) {
        return userRepository.save(User.builder()
                .nickname(nickname)
                .build());
    }

    private Cartoon createToon(User user, String title, String thumbnailPath) {
        return cartoonRepository.save(Cartoon.builder()
                .user(user)
                .title(title)
                .thumbnailPath(thumbnailPath)
                .build());
    }

    private Bookmark createBookmark(Collection collection, Cartoon cartoon) {
        return bookmarkRepository.save(Bookmark.builder()
                .collection(collection)
                .cartoon(cartoon)
                .build());
    }
}