CREATE OR REPLACE FUNCTION check_game_active_and_userid()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT (NEW.deliverytime BETWEEN (SELECT windowstart FROM game WHERE gameid = NEW.gameid) AND (SELECT windowclose FROM game WHERE gameid = NEW.gameid)) THEN
        RAISE EXCEPTION 'Delivery time % is not between start and closing window range of the game ( % and % )', NEW.deliverytime, OLD.windowstart, OLD.windowclose USING ERRCODE = 'ERRG0';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_game_active_and_userid_trigger
BEFORE UPDATE ON GAME
FOR EACH ROW
EXECUTE FUNCTION check_game_active_and_userid();