package com.enchds.uno;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author mOSU_
 */
public class LocalServer {
    private ArrayList<UnoCard> deck = new ArrayList<>(108);
    private ArrayList<UnoCard> discard = new ArrayList<>(50);
    private ArrayList<UnoCard> hand = new ArrayList<>(7);
    
    class ServerGameThread extends Thread {
        @Override
        public void run() {
            boolean out = false;
            score:
            do {
                // Set up deck
                for (int i = 0; i < 4; i++) {
                    deck.add(new UnoCard(0, CardColour.values()[i]));
                }
                for (int i = 0, j = 0; !(i == 12 && j == 0); j++, j %= 4) {
                    if (j == 0) i++;
                    deck.add(new UnoCard(i, CardColour.values()[j]));
                }
                for (int i = 0, j = 0; !(i == 12 && j == 0); j++, j %= 4) {
                    if (j == 0) i++;
                    deck.add(new UnoCard(i, CardColour.values()[j]));
                }
                for (int i = 0; i < 4; i++) {
                    deck.add(new UnoCard(CardValue.WildCard, CardColour.Wild));
                    deck.add(new UnoCard(CardValue.DrawFour, CardColour.Wild));
                }
                Collections.shuffle(deck);
                // Start discard pile
                while (deck.get(0).value == CardValue.DrawFour) Collections.shuffle(deck);
                // Start game
                boolean calledUno = false;
                boolean cardPlayed = true;
                discard.add(deck.get(0));
                deck.remove(0);
                updateCard();
                // Set up hands
                for (int j = 0; j < 7; j++) {
                    hand.add(deck.get(0));
                    deck.remove(0);
                }
                if (discard.get(0).value == CardValue.DrawTwo)
                    for (int i = 0; i < 2; i++) {
                        hand.add(deck.get(0));
                        deck.remove(0);
                    }
                game:
                while (!out) {
                    if (deck.isEmpty()) {
                        while (discard.size() > 1) {
                            if (discard.get(discard.size() - 1).value == CardValue.WildCard
                                    || discard.get(discard.size() - 1).value == CardValue.DrawFour) {
                                discard.get(discard.size() - 1).colour = CardColour.Wild;
                            }
                            deck.add(discard.get(discard.size() - 1));
                            discard.remove(discard.size() - 1);
                        }
                        Collections.shuffle(deck);
                    }
                    String[] options = new String[hand.size() + (deck.isEmpty() ? 1 : 2)];
                    options[0] = "CallUno";
                    for (int i = 0; i < hand.size(); i++) {
                        options[i + 1] = hand.get(i).toString();
                    }
                    if (!deck.isEmpty()) options[hand.size() + 1] = "DrawPile";
                    int card = waitForInt(deck.isEmpty() ? "EmptyDeck" : "WaitForAction", options) - 1;
                    if (card == hand.size()) {
                        if (!deck.isEmpty()) {
                            if (cardIsUsable(deck.get(0), discard.get(0))) {
                                checkInput:
                                while (true) {
                                    int input = waitForInt("DrawCard",
                                            new String[] { "CallUno", deck.get(0).toString(), "NoPlay" });
                                    switch (input) {
                                        case 0:
                                            discard.add(0, deck.get(0));
                                            deck.remove(0);
                                            calledUno = true;
                                            cardPlayed = true;
                                            break checkInput;
                                        case 1:
                                            discard.add(0, deck.get(0));
                                            deck.remove(0);
                                            calledUno = false;
                                            cardPlayed = true;
                                            break checkInput;
                                        case 2:
                                            hand.add(0, deck.get(0));
                                            deck.remove(0);
                                            cardPlayed = false;
                                            break checkInput;
                                    }
                                }
                            } else {
                                hand.add(deck.get(0));
                                deck.remove(0);
                                cardPlayed = false;
                            }
                        }
                    } else if (card == -1) {
                        calledUno = true;
                        continue;
                    } else if (cardIsUsable(hand.get(card), discard.get(0))) {
                        discard.add(0, hand.get(card));
                        hand.remove(card);
                        cardPlayed = true;
                    } else {
                        continue;
                    }
                    if (cardPlayed) updateCard();
                    if (!calledUno & hand.size() == 1) {
                        for (int i = 0; i < 2; i++) {
                            hand.add(deck.get(0));
                            deck.remove(0);
                        }
                        tell("UnoWarning");
                        // You failed to call uno, so you must draw two cards.
                    }
                    if (discard.get(0).value == CardValue.DrawTwo & cardPlayed) {
                        for (int i = 0; i < 2; i++) {
                            if (!deck.isEmpty()) {
                                hand.add(deck.get(0));
                                deck.remove(0);
                            }
                        }
                    }
                    if (discard.get(0).value == CardValue.DrawFour & cardPlayed) {
                        for (int i = 0; i < 4; i++) {
                            if (!deck.isEmpty()) {
                                hand.add(deck.get(0));
                                deck.remove(0);
                            }
                        }
                    }
                    if (hand.isEmpty()) {
                        out = true;
                    }
                    if (discard.get(0).colour == CardColour.Wild) {
                        discard.get(0).setWildColour();
                        updateCard();
                    }
                    calledUno = false;
                }
                out = false;
                deck.clear();
                discard.clear();
                hand.clear();
            } while (true);
        }
    }
    
    private boolean cardIsUsable(UnoCard cardToUse, UnoCard discardTop) {
        if (cardToUse.colour == CardColour.Wild) return true;
        if (discardTop.colour == CardColour.Wild) return true;
        if (cardToUse.colour == discardTop.colour) return true;
        return cardToUse.value == discardTop.value;
    }
    
    private int waitForInt(String reason, String[] options) {
        String print;
        print = reason + "/";
        for (int i = 0; i < options.length; i++) {
            print += options[i];
            if (i != options.length - 1) print += "-";
        }
        UNO.line = print;
        String input;
        while ((input = UNO.out) == null) System.out.print("");
        UNO.out = null;
        return Integer.parseInt(input);
    }
    
    private void tell(String msg) {
        UNO.line = "MSG:" + msg;
        while (UNO.is == null && UNO.line != null) System.out.print("");
    }
    
    private void updateCard() {
        UNO.line = "CARD:" + discard.get(0).toString();
        while (UNO.is == null && UNO.line != null) System.out.print("");
    }
    
    private class UnoCard {
        CardValue value;
        CardColour colour;

        UnoCard(CardValue value, CardColour colour) {
            this.value = value;
            this.colour = colour;
        }

        UnoCard(int value, CardColour colour) {
            this.value = CardValue.values()[value];
            this.colour = colour;
        }

        @Override
        public String toString() {
            if (colour == CardColour.Wild) return value.toString();
            return colour + " " + value;
        }

        boolean setWildColour() {
            checkInput:
            while (true) {
                int newColour = waitForInt("WildColour", new String[] { "Red WildCard", "Green WildCard",
                    "Yellow WildCard", "Blue WildCard" });
                // Please select a colour.
                switch (newColour) {
                    case Integer.MIN_VALUE + 2:
                        return false;
                    case 0:
                        colour = CardColour.Red;
                        return true;
                    case 1:
                        colour = CardColour.Green;
                        return true;
                    case 2:
                        colour = CardColour.Yellow;
                        return true;
                    case 3:
                        colour = CardColour.Blue;
                        return true;
                }
            }
        }
    }
    
    private enum CardValue {
        Zero, One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Skip, Reverse, DrawTwo, WildCard, DrawFour
    }

    private enum CardColour {
        Red, Yellow, Green, Blue, Wild
    }
}