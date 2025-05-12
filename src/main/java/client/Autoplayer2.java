package client;

import java.util.List;

public class Autoplayer2 {

    public static void main(String[] args) throws Exception {
        String baseUrl = "http://euclid.knox.edu:8080/api/blackjack";
        String username = "grwaqo"; // your username
        String password = "ebaac79"; // your password

        ClientConnecter clientConnecter = new ClientConnecter(baseUrl, username, password);
        GameState state = clientConnecter.startGame();

        int maxGames = 1000;
        int gamesPlayed = 0;
        int numWins = 0;
        int numLosses = 0;
        int numPushes = 0;
        int numBlackjacks = 0;
        int numDealerBlackjacks = 0;
        int runningCount = 0;
        int minBet = 5;

        while (gamesPlayed < maxGames){
            gamesPlayed++;

            // BET STRATEGY
            int bet;
            if (runningCount > 5) bet = 50;
            else if (runningCount < -5) bet = 5;
            else bet = 10;

            //if (state.balance < bet) bet = minBet;
            state = clientConnecter.placeBet(state.sessionId, bet);
            if (state.phase == null || !state.phase.equals("RESOLVED")) {
                state = clientConnecter.resumeSession(state.sessionId);
            }

            // CARD COUNT: Initial cards
            runningCount = updateRunningCount(state.playerCards, runningCount);
            runningCount = updateRunningCount(state.dealerCards, runningCount);

            // Instant resolve
            System.out.println(state.phase);
            if ("RESOLVED".equals(state.phase)) {
                // check for blackjack
                if (state.outcome.equals("PLAYER_BLACKJACK")) {
                    numBlackjacks++;
                    numWins++;
                } else if (state.outcome.equals("DEALER_WINS")) {
                    numDealerBlackjacks++;
                    numLosses++;
                } else if (state.outcome.equals("PUSH")) {
                    numPushes++;
                }
                state = clientConnecter.newGame(state.sessionId);
                continue;
            }

            // HIT/STAND STRATEGY
            while (state.playerValue < 17 && state.canHit) {
                if (state.playerValue >= 13 && runningCount >= 6) break;
                state = clientConnecter.hit(state.sessionId);
                runningCount = updateRunningCount(state.playerCards.subList(state.playerCards.size() - 1, state.playerCards.size()), runningCount);
                
            }

            if (state.canStand) {
                state = clientConnecter.stand(state.sessionId);
            }
            if (state.phase == null || !state.phase.equals("RESOLVED")) {
                state = clientConnecter.resumeSession(state.sessionId);
            }

            // FINAL outcome
            if ("RESOLVED".equals(state.phase)) {
                // check for blackjack
                if (state.outcome.equals("PLAYER_WINS")) {
                    numWins++;
                } else if (state.outcome.equals("DEALER_WINS")) {
                    numLosses++;
                } else if (state.outcome.equals("PUSH")) {
                    numPushes++;
                }
            }

            // CARD COUNT: Dealer full hand (if resolved)
            runningCount = updateRunningCount(state.dealerCards, runningCount);
            clientConnecter.newGame(state.sessionId);
        }

        clientConnecter.finishGame(state.sessionId);

        System.out.println("Number of games played: " + gamesPlayed);
        System.out.println("Number of wins: " + numWins);
        System.out.println("Number of losses: " + numLosses);
        System.out.println("Number of pushes: " + numPushes);
        System.out.println("Number of blackjacks: " + numBlackjacks);
        System.out.println("Number of dealer blackjacks: " + numDealerBlackjacks);
        System.out.println("Final balance: " + state.balance);
        System.out.println("Final running count: " + runningCount);
    }

    // Count scoring logic
    public static int scoreCard(String card) {
        int value = check_value(card);
        //System.out.println(value+card);
        switch (value) {
            case 1: return -2;  // Ace
            case 9: case 10: return -1;  // 9, 10, face cards
            case 8: return 0;            // neutral
            case 2: case 3: case 4: case 6: case 7: return 1;
            case 5: return 2;  // worst for player
            default:
                return 0;
        }
    }
    
    

    // Updates running count based on a list of cards
    public static int updateRunningCount(List<String> cards, int count) {
        
        if (cards == null) return count;
        for (String card : cards) {
            count += scoreCard(card);
        }
        return count;
    }
    public static int check_value(String card) {
        if (card == null) return 0;
    
        card = card.toUpperCase();
    
        if (card.contains("TWO")) return 2;
        if (card.contains("THREE")) return 3;
        if (card.contains("FOUR")) return 4;
        if (card.contains("FIVE")) return 5;
        if (card.contains("SIX")) return 6;
        if (card.contains("SEVEN")) return 7;
        if (card.contains("EIGHT")) return 8;
        if (card.contains("NINE")) return 9;
        if (card.contains("TEN")) return 10;
        if (card.contains("JACK")) return 10;
        if (card.contains("QUEEN")) return 10;
        if (card.contains("KING")) return 10;
        if (card.contains("ACE")) return 1;  // used specially
    
        return 0;  // unknown or malformed
    }
    
}
