CREATE INDEX submission_title_idx ON Submission(title);
CREATE INDEX submission_artist_idx ON Submission(artist);
CREATE INDEX submission_album_idx ON Submission(album);
CREATE INDEX submission_mbid_idx ON Submission(mbid);
CREATE INDEX submission_time_idx ON Submission(submission_time);
CREATE INDEX submission_all_idx ON Submission(title,artist,album,len,num);
