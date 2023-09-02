INSERT INTO PROFILE_IMAGE(CREATED_DATE_TIME, FILE_NAME, ORIGIN_FILE_NAME, PATH)
values ('2023-07-21 00:53:01', '1.png', 'default.png', 'sample-path');

INSERT INTO USERS(name, nickname, tag, email, password, created_date_time, modified_date_time, profile_image_id,
                  status_message, is_influencer)
values ('김지훈', '호토란', '@Hotoran', 'hotoran@gmail.com', '$2a$10$c47rWez20sMUFOFmpJTuVunOlM9RRrvXhzVaEWLrigJO1njNZ/iHu',
        '2023-07-21 00:50:01', '2023-07-21 00:50:01', 1,
        'jihun world!', false),
       ('이용우', '구국', '@timel2ss', 'timel2ss@gmail.com', '$2a$10$c47rWez20sMUFOFmpJTuVunOlM9RRrvXhzVaEWLrigJO1njNZ/iHu',
        '2023-07-21 00:51:01', '2023-07-21 00:51:01', 1,
        'ABCDEFU', false),
       ('김승환', '초코송이00', '@palter00', 'choco@gmail.com', '$2a$10$c47rWez20sMUFOFmpJTuVunOlM9RRrvXhzVaEWLrigJO1njNZ/iHu',
        '2023-07-21 00:52:01', '2023-07-21 00:52:01', 1,
        'Genshin', false),
       ('김영현', '영현0808', '@kimye0808', 'king1234@gmail.com',
        '$2a$10$c47rWez20sMUFOFmpJTuVunOlM9RRrvXhzVaEWLrigJO1njNZ/iHu', '2023-07-21 00:53:01', '2023-07-21 00:53:01', 1,
        'leagues', false),
       ('진민서', 'stackoverflowed', '@Iced', 'iced@asd.com',
        '$2a$10$QYTrTjWpvHJs7LFLA6gC2uHE1iZLEa/Wzw5pA.XaHSsyGeVFTykTC', '2023-07-21 00:53:01', '2023-07-21 00:53:01', 1,
        'tft dattak', false);

INSERT INTO FOLLOW(follower_id, followee_id)
values (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (2, 5),
       (5, 2),
       (5, 4);

INSERT INTO AUTHORITY(authority_name)
values ('USER'),
       ('ADMIN');

INSERT INTO USER_AUTHORITIES(user_id, authority_id)
values (1, 2),
       (2, 2),
       (3, 2),
       (4, 2),
       (5, 1);