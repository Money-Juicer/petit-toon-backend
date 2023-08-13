INSERT INTO PROFILE_IMAGE(CREATED_DATE_TIME, FILE_NAME, ORIGIN_FILE_NAME, PATH)
values ('2023-07-21 00:53:01', '1.png', 'default.png', 'sample-path');

INSERT INTO USERS(name, nickname, tag, email, password, created_date_time, modified_date_time, profile_image_id, status_message)
values ('김지훈', '호토란', '@Hotoran', 'hotoran@gmail.com', 'q1w2e3r4', '2023-07-21 00:50:01', '2023-07-21 00:50:01', 1, 'jihun world!'),
       ('이용우', '구국', '@timel2ss', 'timel2ss@gmail.com', '12312312', '2023-07-21 00:51:01', '2023-07-21 00:51:01', 1, 'ABCDEFU'),
       ('김승환', '초코송이00', '@palter00', 'choco@gmail.com', 'abcabcab', '2023-07-21 00:52:01', '2023-07-21 00:52:01', 1, 'Genshin'),
       ('김영현', '영현0808', '@kimye0808', 'king1234@gmail.com', '1', '2023-07-21 00:53:01', '2023-07-21 00:53:01', 1, 'leagues');

INSERT INTO FOLLOW(follower_id, followee_id)
values (1, 2),
       (1, 3),
       (1, 4);