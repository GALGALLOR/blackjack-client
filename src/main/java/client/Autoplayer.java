package client;

public class Autoplayer {

    public static void main(String[] args) throws Exception {
        String baseUrl = "http://euclid.knox.edu:8080/api/blackjack";
        String username = "grwaqo"; // replace with your username
        String password = "ebaac79"; // replace with your password from the file posted to Classroom


        ClientConnecter clientConnecter = new ClientConnecter(baseUrl, username, password);
        GameState state = clientConnecter.startGame();

        int numGames = 1000;
        int numWins = 0;
        int numLosses = 0;
        int numPushes = 0;
        int numBlackjacks = 0;
        int numDealerBlackjacks = 0;

        for (int i = 0; i < numGames; i++) {
            // STRATEGY: how much to bet?
            state = clientConnecter.placeBet(state.sessionId, 10);
            // did we instantly win or lose?
        
            if (state.phase.equals("RESOLVED")) {
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
            // STRATEGY: hit or stand?
            while (state.playerValue < 17 && state.canHit) {
                state = clientConnecter.hit(state.sessionId);
            }
            if (state.canStand) {
                state = clientConnecter.stand(state.sessionId);
            }
            if (state.phase.equals("RESOLVED")) {
                // check for blackjack
                if (state.outcome.equals("PLAYER_WINS")) {
                    numWins++;
                } else if (state.outcome.equals("DEALER_WINS")) {
                    numLosses++;
                } else if (state.outcome.equals("PUSH")) {
                    numPushes++;
                }
            }
            clientConnecter.newGame(state.sessionId);
        }
        clientConnecter.finishGame(state.sessionId);
        System.out.println(state.phase);
        System.out.println("Number of games played: " + numGames);
        System.out.println("Number of wins: " + numWins);
        System.out.println("Number of losses: " + numLosses);
        System.out.println("Number of pushes: " + numPushes);
        System.out.println("Number of blackjacks: " + numBlackjacks);
        System.out.println("Number of dealer blackjacks: " + numDealerBlackjacks);
        System.out.println("Final balance: " + state.balance);
    }
    
}