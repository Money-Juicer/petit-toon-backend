package com.petit.toon.service.feed;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FeedService {
    public static final String FEED_KEY_PREFIX = "feed:";
}
