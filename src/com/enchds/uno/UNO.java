package com.enchds.uno;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UNO {
    static BufferedReader is = null;
    static PrintWriter os = null;
    static String line = null;
    static String out = null;
    static final HashMap<String, String> lang = new HashMap<>();
    static String[] names;
    static String resName;
    static String langName;
    static int uiType;
    // 0 = sprite list (default)[-sp]
    // 1 = list                 [-ls]
    // 2 = combo box (legacy)   [-cb]
    // 3 = text (REALLY legacy) [-tx]
    
    private static void printUsage() {
        System.err.println("Usage: java -jar UNO.JAR [flags]");
        System.err.println("Where legal flags are:");
        System.err.println("-local yes\tPlay a local game");
        System.err.println("-nm\t\tName (default: ANONYMOUS)");
        System.err.println("-res\t\tResource pack (default: RESOURCE)");
        System.err.println("-lang\t\tLanguage name (default: RESOURCE)");
        System.err.println("-ui\t\tUI type (default: sp)");
        System.err.println("  -ui sp\t\tsprite list");
        System.err.println("  -ui cb\t\tCombo box");
        System.err.println("  -ui ls\t\tSelection list");
        System.err.println("  -ui tx\t\tCommand line");
        System.err.println("-address\t Server address (default: localhost)");
        System.err.println("-port\t\t Server port (default: 19283)");
        System.err.println("-local yes and -address are mutually exclusive.");
        System.err.println("You can also enter 'flags=…' into FLAGS.CFG.");
        System.err.println("Rightmore flags supersede leftmore flags.");
        System.err.println("In the same manner, command-line flags supersede FLAGS.CFG.");
        // this will not be localised
        System.exit(1);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String[] cfgFlags = new String[0];
        try (InputStream in = new FileInputStream(new File("FLAGS.CFG"))) {
            Properties p = new Properties();
            p.load(in);
            if (p.containsKey("flags")) cfgFlags = p.getProperty("flags", "").split(" ");
        } catch (IOException exc) {}
        String[] allFlags = new String[args.length + cfgFlags.length];
        System.arraycopy(cfgFlags, 0, allFlags, 0, cfgFlags.length);
        System.arraycopy(args, 0, allFlags, cfgFlags.length, args.length);
        int argAddress = -1;
        int argPort = -1;
        String name = "ANONYMOUS";
        resName = "RESOURCE";
        langName = "RESOURCE";
        for (int i = 0; i < allFlags.length; i++) {
            if (i % 2 == 1) printUsage();
            switch (allFlags[i]) {
                case "-local":
                    if (allFlags[i + 1].equalsIgnoreCase("yes")) argAddress = -2;
                    i++;
                    break;
                case "-nm":
                    name = allFlags[i + 1];
                    i++;
                    break;
                case "-res":
                    resName = allFlags[i + 1];
                    i++;
                    break;
                case "-lang":
                    langName = allFlags[i + 1];
                    i++;
                    break;
                case "-ui":
                    switch (args[i + 1]) {
                        case "sp":
                            uiType = 0;
                            break;
                        case "ls":
                            uiType = 1;
                            break;
                        case "cb":
                            uiType = 2;
                            break;
                        case "tx":
                            uiType = 3;
                            break;
                        default:
                            printUsage();
                    }
                    i++;
                    break;
                case "-address":
                    argAddress = i + 1;
                    i++;
                    break;
                case "-port":
                    argPort = i + 1;
                    i++;
                    break;
                default:
                    printUsage();
            }
        }
        try (InputStream resLang = new FileInputStream(new File(resName, langName + ".CFG"))) {
            if (new File(resName, langName + ".CFG").isFile()) {
                Properties resProps = new Properties();
                resProps.load(resLang);
                resProps.keySet().forEach((Object key) -> {
                    lang.put(key.toString(), resProps.getProperty(key.toString()));
                });
            }
        } catch (IOException exc) {}
        try (InputStream internalLang = UNO.class.getResourceAsStream("/com/enchds/uno/resources/" + langName + ".CFG")) {
            Properties internalProps = new Properties();
            internalProps.load(internalLang);
            internalProps.keySet().forEach((Object key) -> {
                lang.putIfAbsent(key.toString(), internalProps.getProperty(key.toString()));
            });
            
        } catch (IOException exc) {}
        LocalServer.ServerGameThread t = new LocalServer().new ServerGameThread();
        if (argAddress != -2) {
            InetAddress address = null;
            try {
                if (argAddress != -1) address = InetAddress.getByName(allFlags[argAddress]);
                else address = InetAddress.getByName("localhost");
            } catch (UnknownHostException ex) {
                Logger.getLogger(UNO.class.getName()).log(Level.SEVERE, null, ex);
            }
            Socket s1 = null;

            try {
                if (argPort != -1)
                    try {
                        s1 = new Socket(address, Integer.parseUnsignedInt(allFlags[argPort])); // You can use static final constant PORT_NUM
                    } catch (NumberFormatException e) {
                        printUsage();
                    }
                else s1 = new Socket(address, 19283);
                is = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                os = new PrintWriter(s1.getOutputStream());
                os.println(name);
                os.flush();
            } catch (IOException e) {
                System.err.println(lang.getOrDefault("NoServer", "NoServer"));
                System.exit(1);
            }
        } else t.start();
        
        
        /* Create and display the form */
        switch (uiType) {
            case 0:
                GameSP sp = new GameSP();
                java.awt.EventQueue.invokeLater(() -> {
                    sp.setVisible(true);
                });
                sp.new SPThread().start();
                break;
            case 1:
                GameLS ls = new GameLS();
                java.awt.EventQueue.invokeLater(() -> {
                    ls.setVisible(true);
                });
                ls.new LSThread().start();
                break;
            case 2:
                GameCB cb = new GameCB();
                java.awt.EventQueue.invokeLater(() -> {
                    cb.setVisible(true);
                });
                cb.new CBThread().start();
                break;
            case 3:
                GameTX.main();
                break;
        }
    }
}