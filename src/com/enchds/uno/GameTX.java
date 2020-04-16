package com.enchds.uno;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameTX {
    public static void main() {
        while (true)
            try {
                if (UNO.is != null)
                    do UNO.line = UNO.is.readLine();
                    while (UNO.line == null);
                else while (UNO.line == null) System.out.print("");
                if (UNO.line.startsWith("MSG:DESC:"))
                    System.out.println("\"" + UNO.line.replace("MSG:DESC:", "") + "\"");
                else if (UNO.line.startsWith("MSG:NAME:")) UNO.names = UNO.line.replace("MSG:NAME:", "").split("-");
                else if (UNO.line.startsWith("MSG:")) {
                    if (UNO.line.startsWith("MSG:NewPlayer")) {
                        UNO.line = UNO.lang.getOrDefault("NewPlayer", "NewPlayer")
                                .replaceFirst("%", Integer.toString(
                                        Integer.parseInt(UNO.line.substring(UNO.line.length() - 4,
                                                UNO.line.length() - 2)) + 1))
                                .replaceFirst("%", Integer.toString(
                                        Integer.parseInt(UNO.line.substring(UNO.line.length() - 2))))
                                .substring(0, UNO.line.length() - 4);
                        continue;
                    } else if (UNO.line.startsWith("MSG:NextTurn")) {
                        UNO.line = UNO.lang.getOrDefault("NextTurn", "NextTurn")
                                .replaceFirst("%", UNO.names[Integer.parseInt(UNO.line.substring(UNO.line.length() - 2))])
                                .substring(0, UNO.line.length() - 2);
                        continue;
                    }
                    UNO.lang.entrySet().forEach((Map.Entry<String, String> entry) -> {
                        UNO.line = UNO.line.replace(entry.getKey(), entry.getValue());
                    });
                    if (UNO.line.contains("%")) {
                        try {
                            if (UNO.line.replaceFirst("%", "").contains("%")) {
                                UNO.line = UNO.line.replace("%", Integer.toString(Integer.parseInt(
                                            UNO.line.substring(UNO.line.length() - 4, UNO.line.length() - 2))));
                                UNO.line = UNO.line.substring(0, UNO.line.length() - 4)
                                        + UNO.line.substring(UNO.line.length() - 2);
                            }
                            UNO.line = UNO.line.replaceFirst("%",
                                    Integer.toString(Integer.parseInt(UNO.line.substring(UNO.line.length() - 2)) + 1));
                            UNO.line = UNO.line.substring(0, UNO.line.length() - 2);
                        } catch (NumberFormatException exc) {
                            System.err.println("Illegal % in language file! Keeping as is.");
                        }
                    }
                    System.out.println(UNO.line.replace("MSG:", ""));
                } else if (UNO.line.startsWith("CARD:")) {
                    UNO.lang.entrySet().forEach((Map.Entry<String, String> entry) -> {
                        UNO.line = UNO.line.replace(entry.getKey(), entry.getValue());
                    });
                    System.out.println(UNO.lang.getOrDefault("NewCard", "NewCard") + UNO.line.replace("CARD:", ""));
                } else if (UNO.line.startsWith("SCORE:")) {
                    HashMap<String, Integer> scores = new HashMap<>(UNO.line.substring(6).split("-").length);
                    for (int i = 0; i < UNO.line.substring(6).split("-").length; i++)
                        scores.put(Integer.parseInt(UNO.line.substring(6).split("-")[i]) + ":" + i, i);
                    String scoreList = "";
                    for (int i = scores.size() - 1; i >= 0; i--) {
                        scoreList += UNO.lang.getOrDefault("ScoreEntry", "ScoreEntry")
                                .replaceFirst("%1", Integer.toString(scores.size() - i))
                                .replaceFirst("%2", UNO.names[scores.get(scores.keySet().toArray()[i].toString())])
                                .replaceFirst("%3", scores.keySet().toArray()[i].toString().split(":")[0]) + "\n";
                    }
                    System.out.println(UNO.lang.getOrDefault("Scores", "Scores"));
                    System.out.println(scoreList);
                } else {
                    UNO.lang.entrySet().forEach((Map.Entry<String, String> entry) -> {
                        UNO.line = UNO.line.replace(entry.getKey(), entry.getValue());
                    });
                    Scanner s = new Scanner(System.in);
                    System.out.println(UNO.line.split("/")[0]);
                    for (int i = 0; i < UNO.line.split("/")[1].split("-").length; i++) {
                        System.out.println(i + ": " + UNO.line.split("/")[1].split("-")[i]);
                    }
                    System.out.print(UNO.lang.getOrDefault("WaitForInput", "WaitForInput"));
                    if (UNO.os != null){
                        UNO.os.println(s.nextInt());
                        UNO.os.flush();
                    } else UNO.out = Integer.toString(s.nextInt());
                }
                if (UNO.is == null) UNO.line = null;
            } catch (IOException exc) {
                Logger.getLogger(GameTX.class.getName()).log(Level.SEVERE, null, exc);
            }
    }
}