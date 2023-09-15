CREATE OR REPLACE FUNCTION check_gameid_and_guesstime()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT (NEW.guesstime BETWEEN (SELECT windowstart FROM game WHERE gameid = NEW.gameid) AND (SELECT windowclose FROM game WHERE gameid = NEW.gameid)) THEN
        RAISE EXCEPTION 'Guess time % is not between start and closing window range of the game', NEW.guesstime USING ERRCODE = 'ERRA1';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_gameid_and_guesstime_trigger
BEFORE INSERT ON GUESS
FOR EACH ROW
EXECUTE FUNCTION check_gameid_and_guesstime();