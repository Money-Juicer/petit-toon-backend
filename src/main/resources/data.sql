INSERT INTO USERS(name, nickname, email, password, created_date_time, modified_date_time, profile_image_id)
values ('김지훈', '@Hotoran', 'hotoran@gmail.com', 'q1w2e3r4', '2023-07-21 00:50:01', '2023-07-21 00:50:01', 0),
       ('이용우', '@timel2ss', 'timel2ss@gmail.com', '12312312', '2023-07-21 00:51:01', '2023-07-21 00:51:01', 0),
       ('김승환', '@초코송이00', 'choco@gmail.com', 'abcabcab', '2023-07-21 00:52:01', '2023-07-21 00:52:01', 0),
       ('김영현', '@kinggggg', 'king1234@gmail.com', '1', '2023-07-21 00:53:01', '2023-07-21 00:53:01', 0);

INSERT INTO FOLLOW(follower_id, followee_id)
values (1, 2),
       (1, 3),
       (1, 4);
