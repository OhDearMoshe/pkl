# PKL - Paketliga for Discord
![img.png](img.png)

PKL is a bot for guessing package delivery.

## Usage

Replace the placeholder in `.env` file, namely the `BOT_TOKEN`, `SERVER_ID`, `POSTGRES_JDBC_URL` and `POSTGRES_PASSWORD`. As this is not a global bot (for now?) and we haven't really configure it to have different database for different servers, then the settings should really be personalised for each server that is going to use the bot, hence, different database as well.

Afterwards, just run up `Main.kt` and the bot will be good to go.

## Commands
Note: Any time-related parameters are equipped with Natural Language date/time parser. While it is possible to just go with "<b>18:00</b>" as input, the bot would prefer a much verbose input. Here are some examples:
- "Today 18:00" - Will translated to today's date at 18:00
- "24 May 2023 at 18:00" - Self-explanatory
- "18:00" - Ambiguous, could be for tomorrow or today depends on whether the time has passed or not
- "24 May at 19:00" - If the month has passed, this could be May next year
- etc, try it yourself, the result may be different depends on your input

List of available commands goes below. <br/>

### `/paketliga`
Create a PKL game, with parameters:
- `startwindow` - Start window of the delivery time
- `closewindow` - Closing window of the delivery time
- `guessesclose` - Guessing deadline for guessers
- `gamename (optional)` - Game name for the created game.

### `/findgames`
Find <b>active</b> game(s), with parameters:
- `gamecreator (optional)` - The creator of the game
- `gameid (optional)` - ID of the game
- `gamename (optional)` - Name of the game

Leaving all the parameters empty will resulted in the bot listing all active games.

### `/guessgame`
Guess an active game, with parameters:
- `gameid` - ID of the game
- `guesstime` - Guessed delivery time

Guessing a game twice with same game from a same user will resulted in updated guess instead of a new one. </br>
Guessing a game with guess time outside delivery window will resulted in failed command.

### `/findguess`
Find guess(es) from game(s), with parameters:
- `guessid (optional)` - ID of the guess
- `gameid (optional)` - ID of the game

Leaving all the parameters empty will resulted in empty response (for now)

### `/updategame`
Update an active game, with parameters:
- `gameid` - ID of the game
- `startwindow (optional)` - Start window of the delivery time
- `closewindow (optional)` - Closing window of the delivery time
- `guessesclose (optional)` - Guessing deadline for guessers

Leaving all optional parameters empty will resulted in no games to be updated.<br/>
Updating a game will resulted in all guessers to be notified (if any) to update their guesses.

### `/endgame`
End an active game, with parameters:
- `gameid` - ID of the game
- `deliverytime` - Actual delivery time for the game

Ending a game with delivery time outside the window will resulted in failed command.
Ending a game will notify the winning guessers.

### `/leaderboard`
Show leaderboard sorted by total points descending, with parameters:
- `username (optional)` - Username of the user

Leaving the parameter empty will resulted in all (played) users to be shown.
The fields shown are: played, win, lost, and total points.

## TODOs
While the bot already have commands from creating the game, guessing, ending the game, and leaderboard, there are several features that still under developed based on the rules that was made up by r/London discord peeps:

- [ ] Bonus points for winning player, when their submitted guess is the same as the actual delivery time.
- [ ] Splitting points between the players (I assume maximum two players in this case), so 0.5 each for them.
- [ ] If the delivery time is before the guesses close time, the game should be null and void.

## Contributing / Bug report
Please open an Issue / PR, and we'll address it soon. 




