DROP SCHEMA IF EXISTS project22;
CREATE SCHEMA project22;
USE project22;

DROP TABLE IF EXISTS activities;
CREATE TABLE IF NOT EXISTS activities (
activity_id TINYINT NOT NULL AUTO_INCREMENT,
activity_name VARCHAR(45) NOT NULL,
PRIMARY KEY(activity_id)
);

DROP TABLE IF EXISTS dacs_timetable;
CREATE TABLE IF NOT EXISTS dacs_timetable (
datum DATE,
day_name VARCHAR(10) NOT NULL,
start_time INT NOT NULL,
end_time INT NOT NULL,
activity_id TINYINT NOT NULL,
PRIMARY KEY(activity_id),
FOREIGN KEY(activity_id) REFERENCES activities(activity_id)
);

DROP TABLE IF EXISTS questions;
CREATE TABLE IF NOT EXISTS questions (
question VARCHAR(100) NOT NULL,
question_id TINYINT NOT NULL AUTO_INCREMENT,
PRIMARY KEY(question_id)
);

DROP TABLE IF EXISTS courses;
CREATE TABLE IF NOT EXISTS courses (
course_id VARCHAR(10) NOT NULL,
course_name VARCHAR(45) NOT NULL,
PRIMARY KEY(course_id)
);


DROP TABLE IF EXISTS grades;
CREATE TABLE IF NOT EXISTS grades(
course_id VARCHAR(7) NOT NULL,
grade TINYINT NOT NULL,
PRIMARY KEY(course_id),
FOREIGN KEY(course_id) REFERENCES courses(course_id)
);

insert into courses(course_id,course_name) values ("KEN2420","Theoretical Computer Science"), ("KEN2430","Mathematical Modelling");
insert into grades(course_id,grade) values ("KEN2420", 8), ("KEN2430", 9);
insert into activities(activity_name) values ("dsai"), ("ssa"), ("mm");
insert into dacs_timetable(day_name, start_time, end_time, activity_id) values ("Monday", 13, 15, 2), ("Monday", 14,16,3),("Tuesday",2,7,1);